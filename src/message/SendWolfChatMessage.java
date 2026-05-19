package src.message;

public class SendWolfChatMessage {
    public static final String MessageType = "send_wolf_chat";
    public String message_type = MessageType;
    public String roomId;
    public String senderName;
    public String text;

    public SendWolfChatMessage() {}
}
