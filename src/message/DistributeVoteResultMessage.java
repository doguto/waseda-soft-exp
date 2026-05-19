package src.message;

import java.util.Map;

public class DistributeVoteResultMessage {
    public static final String MessageType = "distribute_vote_result";
    public String message_type = MessageType;
    public String targetName;
    public Map<String, Integer> voteCounts;

    public DistributeVoteResultMessage() {}
    public DistributeVoteResultMessage(String targetName, Map<String, Integer> voteCounts) {
        this.targetName = targetName;
        this.voteCounts = voteCounts;
    }
}
