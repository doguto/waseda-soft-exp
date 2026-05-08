package src.message;

public class EndDiscussionMessage {
    public static final String MessageType = "end_discussion";
    public String message_type = MessageType;
    public String roomId;
    public String playerId;

    public EndDiscussionMessage() {}
}
