package src.server.database.entity;

public class ChatMessage {
    public final String senderId;
    public final String senderName;
    public final String text;

    public ChatMessage(String senderId, String senderName, String text) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
    }
}
