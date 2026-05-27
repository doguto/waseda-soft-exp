package src.message;

public class SeerResultMessage {
    public static final String MessageType = "seer_result";
    public String message_type = MessageType;
    public boolean success;
    public String targetName;
    public boolean isWolf;

    public SeerResultMessage() {}
    public SeerResultMessage(String targetName, boolean isWolf) {
        this.success = true;
        this.targetName = targetName;
        this.isWolf = isWolf;
    }
    public SeerResultMessage(boolean success) {
        this.success = success;
    }
}
