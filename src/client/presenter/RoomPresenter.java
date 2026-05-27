package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.network.MessageReceiver;
import src.client.network.ServerConnection;
import src.common.GamePhase;
import src.common.Role;
import src.client.state.GameState;
import src.message.*;

import java.util.concurrent.CompletableFuture;

public class RoomPresenter {
    private final GameState state;
    private final GameSession session;
    private MessageDispatcher dispatcher;

    public RoomPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    public void setDispatcher(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /** 接続してルーム作成/参加リクエストを送信。応答の success/failure を Boolean Future で返す。 */
    public CompletableFuture<Boolean> connect(String host, String name, String room, boolean isCreate) {
        try {
            session.setConnection(new ServerConnection(host, 8080));
            state.myName = name;
            state.roomId = room;
            new MessageReceiver(session.getConnection(), dispatcher).start();

            Object msg;
            String responseType;
            if (isCreate) {
                CreateRoomMessage m = new CreateRoomMessage();
                m.roomId = room; m.name = name;
                msg = m;
                responseType = CreateRoomResultMessage.MessageType;
            } else {
                JoinRoomMessage m = new JoinRoomMessage();
                m.roomId = room; m.name = name;
                msg = m;
                responseType = JoinRoomResultMessage.MessageType;
            }

            return session.sendRequest(msg, responseType)
                .thenApply(node -> {
                    boolean success = node.get("success").asBoolean();
                    if (success) {
                        log("[システム] ルームを" + (isCreate ? "作成" : "参加") + "しました: " + state.roomId);
                        state.notifyListeners();
                        return true;
                    } else {
                        // サーバー側のエラーメッセージを例外として伝播させ、LobbyPanel 側で表示させる
                        String msgText = node.has("message") ? node.get("message").asText() : "ルーム参加に失敗しました";
                        throw new RuntimeException(msgText);
                    }
                });
        } catch (Exception e) {
            CompletableFuture<Boolean> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    /** ゲーム開始リクエストを送信。応答の success/failure を Boolean Future で返す。 */
    public CompletableFuture<Boolean> requestStartGame() {
        StartGameMessage m = new StartGameMessage();
        m.roomId = state.roomId;
        return session.sendRequest(m, StartGameResultMessage.MessageType)
            .thenApply(node -> {
                boolean ok = node.get("success").asBoolean();
                if (!ok) {
                    log("[エラー] ゲーム開始失敗: " + node.get("message").asText());
                    state.notifyListeners();
                }
                return ok;
            });
    }

    // --- サーバーからの自発的なブロードキャストハンドラ ---

    public void onDistributeRole(JsonNode node) {
        state.myRole = Role.valueOf(node.get("role").asText());
        state.players.clear();
        state.deadPlayers.clear();
        JsonNode playersNode = node.get("player_names");
        if (playersNode != null && playersNode.isArray()) {
            for (JsonNode playerNode : playersNode) {
                state.players.add(playerNode.asText());
            }
        }
        // サーバーが初日の配役時に昼へ遷移させる場合は DistributeRoleMessage.startDay が true になる
        JsonNode startDayNode = node.get("startDay");
        if (startDayNode != null && startDayNode.asBoolean(false)) {
            int alive = state.players.size();
            state.endDiscussionFor = 0;
            state.endDiscussionAlive = alive;
            state.endDiscussionNeed = (alive / 2) + 1;
            state.phase = GamePhase.DAY_DISCUSSION;
        } else {
            state.phase = GamePhase.NIGHT;
        }
        log("[システム] ゲーム開始！ あなたの役職: 【" + state.myRole + "】");
        state.notifyListeners();
    }

    public void onNightPhaseStart(JsonNode node) {
        state.phase = GamePhase.NIGHT;
        log("[システム] 夜フェーズが開始しました");
        state.notifyListeners();
    }

    public void onVotePhaseStart(JsonNode node) {
        state.phase = GamePhase.DAY_VOTE;
        log("[システム] 投票フェーズが開始しました");
        state.notifyListeners();
    }

    public void onAnnounceMorning(JsonNode node) {
        JsonNode deadNode = node.get("deadPlayerName");
        if (deadNode != null && !deadNode.isNull()) {
            String dead = deadNode.asText();
            if (!state.deadPlayers.contains(dead)) state.deadPlayers.add(dead);
            if (dead.equals(state.myName)) {
                state.isAlive = false;
            }
            log("[朝] " + dead + " が死亡しました...");
        } else {
            log("[朝] 誰も死亡しませんでした（守護成功）");
        }
        state.phase = GamePhase.DAY_DISCUSSION;
        state.notifyListeners();
    }

    public void onRoomSnapshot(JsonNode node) {
        // players
        state.players.clear();
        JsonNode playersNode = node.get("players");
        if (playersNode != null && playersNode.isArray()) {
            for (JsonNode p : playersNode) state.players.add(p.asText());
        }
        // deadPlayers
        state.deadPlayers.clear();
        JsonNode deadNode = node.get("deadPlayers");
        if (deadNode != null && deadNode.isArray()) {
            for (JsonNode d : deadNode) state.deadPlayers.add(d.asText());
        }
        // role & alive
        if (node.has("myRole") && !node.get("myRole").isNull()) {
            state.myRole = src.common.Role.valueOf(node.get("myRole").asText());
        }
        if (node.has("isAlive")) state.isAlive = node.get("isAlive").asBoolean();
        // phase
        if (node.has("phase")) state.phase = src.common.GamePhase.valueOf(node.get("phase").asText());

        // chat logs
        state.chatLog.clear(); state.wolfChatLog.clear(); state.graveChatLog.clear();
        JsonNode v = node.get("villageChat");
        if (v != null && v.isArray()) for (JsonNode c : v) state.chatLog.add(c.asText());
        JsonNode w = node.get("wolfChat");
        if (w != null && w.isArray()) for (JsonNode c : w) state.wolfChatLog.add(c.asText());
        JsonNode g = node.get("graveChat");
        if (g != null && g.isArray()) for (JsonNode c : g) state.graveChatLog.add(c.asText());

        log("[システム] ルーム状態を復元しました");
        state.notifyListeners();
    }

    public void onDayPhaseStart(JsonNode node) {
        // 明示的な昼開始メッセージを受け取ったらクライアントを昼フェーズに設定
        JsonNode votesForNode = node.get("votesFor");
        JsonNode aliveCountNode = node.get("aliveCount");
        JsonNode needNode = node.get("need");
        if (votesForNode != null) {
            state.endDiscussionFor = votesForNode.asInt();
        }
        if (aliveCountNode != null) {
            state.endDiscussionAlive = aliveCountNode.asInt();
        }
        if (needNode != null) {
            state.endDiscussionNeed = needNode.asInt();
        }
        state.phase = GamePhase.DAY_DISCUSSION;
        log("[システム] 昼フェーズ（議論）が開始しました");
        state.notifyListeners();
    }

    public void onExecute(JsonNode node) {
        String executed = node.get("executedPlayerName").asText();
        String role = node.get("executedRole").asText();
        if (!state.deadPlayers.contains(executed)) state.deadPlayers.add(executed);
        if (executed.equals(state.myName)) {
            state.isAlive = false;
        }
        if (!state.lastVoteTieCandidates.isEmpty()) {
            // 同票で抽選が入った場合は抽選結果という表現にする
            log("[システム] 抽選の結果、[" + executed + "] が処刑されました。");
            // リセット
            state.lastVoteTieCandidates.clear();
            state.lastVoteTopCount = 0;
        } else {
            log("[処刑] " + executed + "（" + role + "）が処刑されました");
        }
        state.phase = GamePhase.NIGHT;
        state.notifyListeners();
    }

    public void onAnnounceGameOver(JsonNode node) {
        String winner = node.get("winner").asText();
        log("[ゲーム終了] 勝者: " + winner);
        state.phase = GamePhase.GAME_OVER;
        state.notifyListeners();
    }

    public void onDisconnect() {
        state.phase = GamePhase.LOBBY;
        log("[システム] サーバーから切断されました");
        state.notifyListeners();
    }

    private void send(Object msg) {
        session.send(msg);
    }

    private void log(String msg) {
        state.chatLog.add(msg);
    }
}
