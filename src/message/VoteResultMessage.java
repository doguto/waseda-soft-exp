package src.message;

public class VoteResultMessage {
    public static final String MessageType = "vote_result";
    public String message_type = MessageType;
    public boolean success;

    public VoteResultMessage() {}
    public VoteResultMessage(boolean success) { this.success = success; }
}
