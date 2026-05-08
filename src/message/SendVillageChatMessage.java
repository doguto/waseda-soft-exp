package src.message;

public class SendVillageChatMessage {
    public static final String MessageType = "send_village_chat";
    public String message_type = MessageType;
    public String roomId;
    public String senderName;
    public String text;

    public SendVillageChatMessage() {}
}
