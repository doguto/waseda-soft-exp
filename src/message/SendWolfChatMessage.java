package src.message;

public class SendWolfChatMessage {
    public static final String MessageType = "send_wolf_chat";
    public String message_type = MessageType;
    public String roomId;
    public String senderId;
    public String text;

    public SendWolfChatMessage() {}
}
