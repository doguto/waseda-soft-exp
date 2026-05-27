package src.message;

public class EndDiscussionStatusMessage {
    public static final String MessageType = "end_discussion_status";
    public String message_type = MessageType;
    public int votesFor;
    public int aliveCount;
    public int need;

    public EndDiscussionStatusMessage() {}

    public EndDiscussionStatusMessage(int votesFor, int aliveCount, int need) {
        this.votesFor = votesFor;
        this.aliveCount = aliveCount;
        this.need = need;
    }
}
