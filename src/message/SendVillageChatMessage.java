package src.message;

public class SendVillageChatMessage {
    public static final String MessageType = "send_village_chat";
    public String message_type = MessageType;
    public String roomId;
    public String senderId;
    public String text;

    public SendVillageChatMessage() {}
}
