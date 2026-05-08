package src.message;

public class VoteMessage {
    public static final String MessageType = "vote";
    public String message_type = MessageType;
    public String roomId;
    public String playerId;
    public String targetId;

    public VoteMessage() {}
}
