package src.server.database.entity;

public class ChatMessage {
    public final String senderName;
    public final String text;

    public ChatMessage(String senderName, String text) {
        this.senderName = senderName;
        this.text = text;
    }
}
