package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.state.GameState;
import src.message.*;

public class ChatPresenter {
    private final GameState state;
    private final GameSession session;

    public ChatPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    public void sendGeneralChat(String text) {
        // allow send when alive, or when the game is over (spectator chat)
        if (!state.isAlive && state.phase != src.common.GamePhase.GAME_OVER) return;
        SendVillageChatMessage m = new SendVillageChatMessage();
        fill(m, text);
        session.send(m);
    }

    public void sendWolfChat(String text) {
        SendWolfChatMessage m = new SendWolfChatMessage();
        fill(m, text);
        session.send(m);
    }

    public void sendGloveChat(String text) {
        SendGraveChatMessage m = new SendGraveChatMessage();
        fill(m, text);
        session.send(m);
    }

    // 共通フィールドのセット
    private void fill(SendVillageChatMessage m, String text) { m.roomId = state.roomId; m.senderName = state.myName; m.text = text; }
    private void fill(SendWolfChatMessage    m, String text) { m.roomId = state.roomId; m.senderName = state.myName; m.text = text; }
    private void fill(SendGraveChatMessage   m, String text) { m.roomId = state.roomId; m.senderName = state.myName; m.text = text; }

    // --- サーバーメッセージハンドラ ---

    public void onChatBroadcast(JsonNode node) {
        String chatType = node.get("chatType").asText();
        String sender   = node.get("senderName").asText();
        String text     = node.get("text").asText();
<<<<<<< HEAD
        if (isRealParticipant(sender) && !state.players.contains(sender)) {
            state.players.add(sender);
        }
=======
>>>>>>> feature/day-phase-gui
        String line = sender + ": " + text;
        switch (chatType) {
            case "WOLF"  -> state.wolfChatLog.add(line);
            case "GRAVE" -> state.graveChatLog.add(line);
            default      -> state.chatLog.add(line);
        }
        state.notifyListeners();
    }

<<<<<<< HEAD
    private boolean isRealParticipant(String sender) {
        if (sender == null || sender.isBlank()) {
            return false;
        }
        if (sender.startsWith("[") && sender.endsWith("]")) {
            return false;
        }
        return !"NPC".equalsIgnoreCase(sender);
    }
=======
>>>>>>> feature/day-phase-gui
}
