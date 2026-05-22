package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.state.GamePhase;
import src.client.state.GameState;
import src.message.*;

public class ChatPresenter {
    private final GameState state;
    private final GameSession session;

    public ChatPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    public void sendChat(String text) {
        Object msg;
        if ("WOLF".equals(state.myRole) && state.phase == GamePhase.NIGHT) {
            SendWolfChatMessage m = new SendWolfChatMessage();
            m.roomId = state.roomId;
            m.senderName = state.myName;
            m.text = text;
            msg = m;
        } else {
            SendVillageChatMessage m = new SendVillageChatMessage();
            m.roomId = state.roomId;
            m.senderName = state.myName;
            m.text = text;
            msg = m;
        }
        session.send(msg);
    }

    // --- サーバーメッセージハンドラ ---

    public void onChatBroadcast(JsonNode node) {
        String chatType = node.get("chatType").asText();
        String sender   = node.get("senderName").asText();
        String text     = node.get("text").asText();
        if (!state.players.contains(sender)) {
            state.players.add(sender);
        }
        state.chatLog.add("[" + chatType + "] " + sender + ": " + text);
        state.notifyListeners();
    }
}
