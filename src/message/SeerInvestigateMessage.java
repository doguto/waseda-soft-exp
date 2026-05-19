package src.message;

public class SeerInvestigateMessage {
    public static final String MessageType = "seer_investigate";
    public String message_type = MessageType;
    public String roomId;
    public String seerName;
    public String targetName;

    public SeerInvestigateMessage() {}
}
