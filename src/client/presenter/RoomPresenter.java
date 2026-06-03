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
                        // 即時に自分をローカルリストへ追加する（フェーズはサーバーの RoomSnapshot を待つ）
                        if (!state.players.contains(state.myName)) state.players.add(state.myName);
                        state.isHost = isCreate;
                        log("【システム】ルームを" + (isCreate ? "作成" : "参加") + "しました: " + state.roomId);
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
            // 初日は朝フェーズを挟まず昼(議論)から開始する
            state.phase = GamePhase.DAY_DISCUSSION;
        } else {
            state.phase = GamePhase.NIGHT;
        }
        // 初期フラグをリセット
        state.hasVoted = false;
        state.hasNightActionSent = false;
        log("【システム】ゲーム開始。あなたの役職は 【" + state.myRole.displayName() + "】 です。");
        state.notifyListeners();
    }

    public void onNightPhaseStart(JsonNode node) {
        state.phase = GamePhase.NIGHT;
        state.hasNightActionSent = false;
        log("【システム】夜になりました。");
        state.notifyListeners();
    }

    public void onVotePhaseStart(JsonNode node) {
        state.phase = GamePhase.DAY_VOTE;
        state.hasVoted = false;
        log("【システム】投票が始まりました。");
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
            log("【システム】朝になりました。死体が見つかりました: " + dead + "。");
        } else {
            log("【システム】朝になりました。死体はありませんでした。");
        }
        // 朝フェーズへ。一定時間後にサーバーから DayPhaseStart が届き昼へ移行する。
        state.phase = GamePhase.MORNING;
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
        if (node.has("endDiscussionFor")) state.endDiscussionFor = node.get("endDiscussionFor").asInt();
        if (node.has("endDiscussionNeed")) state.endDiscussionNeed = node.get("endDiscussionNeed").asInt();
        if (node.has("endDiscussionAlive")) state.endDiscussionAlive = node.get("endDiscussionAlive").asInt();
        if (node.has("hasVoted")) state.hasVoted = node.get("hasVoted").asBoolean();
        if (node.has("hasNightActionSent")) state.hasNightActionSent = node.get("hasNightActionSent").asBoolean();

        // chat logs
        state.chatLog.clear(); state.wolfChatLog.clear(); state.graveChatLog.clear();
        JsonNode v = node.get("villageChat");
        if (v != null && v.isArray()) for (JsonNode c : v) state.chatLog.add(c.asText());
        JsonNode w = node.get("wolfChat");
        if (w != null && w.isArray()) for (JsonNode c : w) state.wolfChatLog.add(c.asText());
        JsonNode g = node.get("graveChat");
        if (g != null && g.isArray()) for (JsonNode c : g) state.graveChatLog.add(c.asText());

        log("【システム】ルーム状態を復元しました");
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
        // 処刑時に役職は公開しない（役職は霊媒結果やゲーム終了時のみ判明する）
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
            log("[処刑] " + executed + " が処刑されました");
        }
        state.phase = GamePhase.NIGHT;
        state.notifyListeners();
    }

    public void onAnnounceGameOver(JsonNode node) {
        String winner = node.get("winner").asText();
        state.winner = winner;
        boolean wolfCampWon = "WOLF".equals(winner);
        log("[ゲーム終了] 勝者: " + (wolfCampWon ? "人狼陣営" : "村人陣営"));
        // 狂人は人狼陣営として勝敗を判定する（Role.isWolfCamp）
        if (state.myRole != null) {
            boolean iWon = state.myRole.isWolfCamp() == wolfCampWon;
            String camp = state.myRole.isWolfCamp() ? "人狼陣営" : "村人陣営";
            log("[結果] あなたは" + camp + "（" + state.myRole.displayName() + "）。"
                    + (iWon ? "勝利しました！" : "敗北しました…"));
        }
        // 全プレイヤーの役職を公開する
        state.finalRoles.clear();
        JsonNode playersNode = node.get("players");
        if (playersNode != null && playersNode.isArray()) {
            log("── 役職公開 ──");
            for (JsonNode p : playersNode) {
                String pName = p.get("name").asText();
                String pRoleStr = p.get("role").asText();
                String pRoleDisplay;
                try {
                    pRoleDisplay = Role.valueOf(pRoleStr).displayName();
                } catch (IllegalArgumentException e) {
                    pRoleDisplay = pRoleStr;
                }
                state.finalRoles.put(pName, pRoleDisplay);
                log("  " + pName + ": " + pRoleDisplay);
            }
        }
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
