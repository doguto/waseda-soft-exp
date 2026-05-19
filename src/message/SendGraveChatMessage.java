package src.message;

public class SendGraveChatMessage {
    public static final String MessageType = "send_grave_chat";
    public String message_type = MessageType;
    public String roomId;
    public String senderName;
    public String text;

    public SendGraveChatMessage() {}
}
