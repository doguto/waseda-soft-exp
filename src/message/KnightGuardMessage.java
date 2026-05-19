package src.message;

public class KnightGuardMessage {
    public static final String MessageType = "knight_guard";
    public String message_type = MessageType;
    public String roomId;
    public String knightName;
    public String targetName;

    public KnightGuardMessage() {}
}
