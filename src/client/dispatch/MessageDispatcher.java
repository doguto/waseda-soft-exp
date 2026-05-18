package src.client.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.client.state.GamePhase;
import src.client.state.GameState;
import src.message.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MessageDispatcher {
    private final GameState state;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Consumer<JsonNode>> handlers = new HashMap<>();

    public MessageDispatcher(GameState state) {
        this.state = state;
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put(CreateRoomResultMessage.MessageType,    this::onCreateRoomResult);
        handlers.put(JoinRoomResultMessage.MessageType,      this::onJoinRoomResult);
        handlers.put(StartGameResultMessage.MessageType,     this::onStartGameResult);
        handlers.put(DistributeRoleMessage.MessageType,      this::onDistributeRole);
        handlers.put(ChatBroadcastMessage.MessageType,       this::onChatBroadcast);
        handlers.put(SeerResultMessage.MessageType,          this::onSeerResult);
        handlers.put(MediumResultMessage.MessageType,        this::onMediumResult);
        handlers.put(AnnounceMorningMessage.MessageType,     this::onAnnounceMorning);
        handlers.put(AnnounceGameOverMessage.MessageType,    this::onAnnounceGameOver);
        handlers.put(ExecuteMessage.MessageType,             this::onExecute);
        handlers.put(DistributeVoteResultMessage.MessageType,this::onDistributeVoteResult);
        handlers.put(EndDiscussionResultMessage.MessageType, this::onEndDiscussionResult);
        handlers.put(VoteResultMessage.MessageType,          this::onVoteResult);
        handlers.put(WolfAttackResultMessage.MessageType,    this::onWolfAttackResult);
        handlers.put(KnightGuardResultMessage.MessageType,   this::onKnightGuardResult);
        handlers.put(SeerInvestigateMessage.MessageType,     this::onSeerInvestigateResult);
    }

    public void dispatch(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            String type = node.has("message_type") ? node.get("message_type").asText() : null;
            if (type == null) return;
            Consumer<JsonNode> handler = handlers.get(type);
            if (handler != null) {
                handler.accept(node);
            } else {
                log("[システム] 未処理メッセージ: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisconnect() {
        state.phase = GamePhase.LOBBY;
        log("[システム] サーバーから切断されました");
        state.notifyListeners();
    }

    // ── ハンドラ群 ────────────────────────────────────────────────────────────

    private void onCreateRoomResult(JsonNode node) {
        if (node.get("success").asBoolean()) {
            state.players.add(state.myName);
            state.phase = GamePhase.WAITING;
            log("[システム] ルームを作成しました: " + state.roomId);
        } else {
            log("[エラー] " + node.get("message").asText());
        }
        state.notifyListeners();
    }

    private void onJoinRoomResult(JsonNode node) {
        if (node.get("success").asBoolean()) {
            state.players.add(state.myName);
            state.phase = GamePhase.WAITING;
            log("[システム] ルームに参加しました: " + state.roomId);
        } else {
            log("[エラー] " + node.get("message").asText());
        }
        state.notifyListeners();
    }

    private void onStartGameResult(JsonNode node) {
        if (!node.get("success").asBoolean()) {
            log("[エラー] ゲーム開始失敗: " + node.get("message").asText());
            state.notifyListeners();
        }
        // 成功時は distribute_role を待つ（そちらでフェーズ遷移）
    }

    private void onDistributeRole(JsonNode node) {
        state.myRole = node.get("role").asText();
        state.phase = GamePhase.NIGHT;
        log("[システム] ゲーム開始！ あなたの役職: 【" + state.myRole + "】");
        state.notifyListeners();
    }

    private void onChatBroadcast(JsonNode node) {
        String chatType = node.get("chatType").asText();
        String sender = node.get("senderName").asText();
        String text = node.get("text").asText();
        // チャット送信者をプレイヤーリストに追加（初回のみ）
        if (!state.players.contains(sender)) {
            state.players.add(sender);
        }
        log("[" + chatType + "] " + sender + ": " + text);
        state.notifyListeners();
    }

    private void onSeerResult(JsonNode node) {
        String target = node.get("targetName").asText();
        boolean isWolf = node.get("isWolf").asBoolean();
        log("[占い結果] " + target + " は " + (isWolf ? "🐺 人狼！" : "村人側"));
        state.notifyListeners();
    }

    private void onMediumResult(JsonNode node) {
        String target = node.get("targetName").asText();
        boolean isWolf = node.get("isWolf").asBoolean();
        log("[霊媒結果] " + target + " は " + (isWolf ? "🐺 人狼だった！" : "村人側だった"));
        state.notifyListeners();
    }

    private void onAnnounceMorning(JsonNode node) {
        JsonNode deadNode = node.get("deadPlayerName");
        if (deadNode != null && !deadNode.isNull()) {
            String dead = deadNode.asText();
            state.players.remove(dead);
            log("[朝] " + dead + " が死亡しました...");
        } else {
            log("[朝] 誰も死亡しませんでした（守護成功）");
        }
        state.phase = GamePhase.DAY_DISCUSSION;
        state.notifyListeners();
    }

    private void onExecute(JsonNode node) {
        String executed = node.get("executedPlayerName").asText();
        String role = node.get("executedRole").asText();
        state.players.remove(executed);
        log("[処刑] " + executed + "（" + role + "）が処刑されました");
        // announce_game_over が来なければ夜フェーズへ遷移
        state.phase = GamePhase.NIGHT;
        state.notifyListeners();
    }

    private void onAnnounceGameOver(JsonNode node) {
        String winner = node.get("winner").asText();
        log("[ゲーム終了] 勝者: " + winner);
        state.phase = GamePhase.GAME_OVER;
        state.notifyListeners();
    }

    private void onDistributeVoteResult(JsonNode node) {
        String target = node.get("targetName").asText();
        log("[投票集計] 最多票: " + target);
        state.notifyListeners();
    }

    private void onEndDiscussionResult(JsonNode node) {
        if (node.get("success").asBoolean()) {
            state.phase = GamePhase.DAY_VOTE;
            log("[システム] 議論終了 → 投票フェーズへ");
            state.notifyListeners();
        }
    }

    private void onVoteResult(JsonNode node) {
        log("[投票] 投票を受け付けました");
        state.notifyListeners();
    }

    private void onWolfAttackResult(JsonNode node) {
        log("[夜] 襲撃を実行しました");
        state.notifyListeners();
    }

    private void onKnightGuardResult(JsonNode node) {
        log("[夜] 守護しました");
        state.notifyListeners();
    }

    private void onSeerInvestigateResult(JsonNode node) {
        // seer_investigate の結果は seer_result で返る
    }

    private void log(String msg) {
        state.chatLog.add(msg);
    }
}
