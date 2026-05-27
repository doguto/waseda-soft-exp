package src.message;

public class VoteResultMessage {
    public static final String MessageType = "vote_result";
    public String message_type = MessageType;
    public boolean success;
    public String message;

    public VoteResultMessage() {}
    public VoteResultMessage(boolean success) { this.success = success; }
    public VoteResultMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
