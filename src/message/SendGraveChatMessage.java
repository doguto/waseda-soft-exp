package src.message;

public class SendGraveChatMessage {
    public static final String MessageType = "send_grave_chat";
    public String message_type = MessageType;
    public String roomId;
    public String senderId;
    public String text;

    public SendGraveChatMessage() {}
}
