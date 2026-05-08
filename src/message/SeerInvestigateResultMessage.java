package src.message;

public class SeerInvestigateResultMessage {
    public static final String MessageType = "seer_investigate_result";
    public String message_type = MessageType;
    public boolean success;

    public SeerInvestigateResultMessage() {}
    public SeerInvestigateResultMessage(boolean success) { this.success = success; }
}
