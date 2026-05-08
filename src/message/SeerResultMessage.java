package src.message;

public class SeerResultMessage {
    public static final String MessageType = "seer_result";
    public String message_type = MessageType;
    public String targetName;
    public boolean isWolf;

    public SeerResultMessage() {}
    public SeerResultMessage(String targetName, boolean isWolf) {
        this.targetName = targetName;
        this.isWolf = isWolf;
    }
}
