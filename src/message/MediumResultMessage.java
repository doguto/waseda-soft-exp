package src.message;

public class MediumResultMessage {
    public static final String MessageType = "medium_result";
    public String message_type = MessageType;
    public String targetName;
    public boolean isWolf;

    public MediumResultMessage() {}
    public MediumResultMessage(String targetName, boolean isWolf) {
        this.targetName = targetName;
        this.isWolf = isWolf;
    }
}
