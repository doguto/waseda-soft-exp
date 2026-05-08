package src.message;

public class EndDiscussionResultMessage {
    public static final String MessageType = "end_discussion_result";
    public String message_type = MessageType;
    public boolean success;

    public EndDiscussionResultMessage() {}
    public EndDiscussionResultMessage(boolean success) { this.success = success; }
}
