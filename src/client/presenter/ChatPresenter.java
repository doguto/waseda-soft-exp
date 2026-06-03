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
        String chatType = readText(node, "chatType", "VILLAGE");
        String sender   = readText(node, "senderName", "システム");
        String text     = readText(node, "text", "");
        if (isRealParticipant(sender) && !state.players.contains(sender)) {
            state.players.add(sender);
        }
        // システム送信者はチャンネルラベルを付けず、明示的に「【システム】」として表示する
        if ("システム".equals(sender) || "SYSTEM".equalsIgnoreCase(sender) || "System".equals(sender)) {
            String line = "【システム】 " + text;
            state.chatLog.add(line);
        } else {
            String line = "【" + channelLabel(chatType) + "】" + sender + ": " + text;
            switch (chatType) {
                case "WOLF"  -> state.wolfChatLog.add(line);
                case "GRAVE" -> state.graveChatLog.add(line);
                default      -> state.chatLog.add(line);
            }
        }
        state.notifyListeners();
    }

    private String channelLabel(String chatType) {
        return switch (chatType) {
            case "WOLF" -> "人狼";
            case "GRAVE" -> "墓地";
            default -> "全体";
        };
    }

    private String readText(JsonNode node, String fieldName, String fallback) {
        if (node != null && node.hasNonNull(fieldName)) {
            String value = node.get(fieldName).asText();
            if (value != null && !value.isBlank() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return fallback;
    }

    private boolean isRealParticipant(String sender) {
        if (sender == null || sender.isBlank()) {
            return false;
        }
        if (sender.startsWith("[") && sender.endsWith("]")) {
            return false;
        }
        // Exclude known non-player senders (NPC, system notifications)
        if ("NPC".equalsIgnoreCase(sender)) return false;
        if ("システム".equals(sender) || "SYSTEM".equalsIgnoreCase(sender) || "System".equals(sender)) return false;
        return true;
    }
}
