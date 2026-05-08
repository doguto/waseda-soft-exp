package src.message;

public class ChatBroadcastMessage {
    public static final String MessageType = "chat_broadcast";
    public String message_type = MessageType;
    public String chatType; // "VILLAGE", "WOLF", "GRAVE"
    public String senderId;
    public String senderName;
    public String text;

    public ChatBroadcastMessage() {}
    public ChatBroadcastMessage(String chatType, String senderId, String senderName, String text) {
        this.chatType = chatType;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
    }
}
