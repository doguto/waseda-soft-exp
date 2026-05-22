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
                    if (node.get("success").asBoolean()) {
                        state.players.add(state.myName);
                        state.phase = GamePhase.WAITING;
                        log("[システム] ルームを" + (isCreate ? "作成" : "参加") + "しました: " + state.roomId);
                    } else {
                        log("[エラー] " + node.get("message").asText());
                    }
                    state.notifyListeners();
                    return node.get("success").asBoolean();
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
        state.phase = GamePhase.NIGHT;
        log("[システム] ゲーム開始！ あなたの役職: 【" + state.myRole + "】");
        state.notifyListeners();
    }

    public void onAnnounceMorning(JsonNode node) {
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

    public void onExecute(JsonNode node) {
        String executed = node.get("executedPlayerName").asText();
        String role = node.get("executedRole").asText();
        state.players.remove(executed);
        log("[処刑] " + executed + "（" + role + "）が処刑されました");
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
