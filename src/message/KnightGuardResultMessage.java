package src.message;

public class KnightGuardResultMessage {
    public static final String MessageType = "knight_guard_result";
    public String message_type = MessageType;
    public boolean success;

    public KnightGuardResultMessage() {}
    public KnightGuardResultMessage(boolean success) { this.success = success; }
}
