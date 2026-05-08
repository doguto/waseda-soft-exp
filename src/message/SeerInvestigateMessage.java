package src.message;

public class SeerInvestigateMessage {
    public static final String MessageType = "seer_investigate";
    public String message_type = MessageType;
    public String roomId;
    public String seerId;
    public String targetId;

    public SeerInvestigateMessage() {}
}
