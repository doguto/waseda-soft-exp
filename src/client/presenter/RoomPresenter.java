package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.network.MessageReceiver;
import src.client.network.ServerConnection;
import src.client.state.GamePhase;
import src.client.state.GameState;
import src.message.*;

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

    public void connect(String host, String name, String room, boolean isCreate) {
        try {
            session.setConnection(new ServerConnection(host, 8080));
            state.myName = name;
            state.roomId = room;
            new MessageReceiver(session.getConnection(), dispatcher).start();
            Object msg;
            if (isCreate) {
                CreateRoomMessage m = new CreateRoomMessage();
                m.roomId = room;
                m.name = name;
                msg = m;
            } else {
                JoinRoomMessage m = new JoinRoomMessage();
                m.roomId = room;
                m.name = name;
                msg = m;
            }
            session.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void requestStartGame() {
        StartGameMessage m = new StartGameMessage();
        m.roomId = state.roomId;
        send(m);
    }

    // --- サーバーメッセージハンドラ ---

    public void onCreateRoomResult(JsonNode node) {
        if (node.get("success").asBoolean()) {
            state.players.add(state.myName);
            state.phase = GamePhase.WAITING;
            log("[システム] ルームを作成しました: " + state.roomId);
        } else {
            log("[エラー] " + node.get("message").asText());
        }
        state.notifyListeners();
    }

    public void onJoinRoomResult(JsonNode node) {
        if (node.get("success").asBoolean()) {
            state.players.add(state.myName);
            state.phase = GamePhase.WAITING;
            log("[システム] ルームに参加しました: " + state.roomId);
        } else {
            log("[エラー] " + node.get("message").asText());
        }
        state.notifyListeners();
    }

    public void onStartGameResult(JsonNode node) {
        if (!node.get("success").asBoolean()) {
            log("[エラー] ゲーム開始失敗: " + node.get("message").asText());
            state.notifyListeners();
        }
    }

    public void onDistributeRole(JsonNode node) {
        state.myRole = node.get("role").asText();
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
