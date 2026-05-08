package src.message;

public class WolfAttackMessage {
    public static final String MessageType = "wolf_attack";
    public String message_type = MessageType;
    public String roomId;
    public String wolfId;
    public String targetId;

    public WolfAttackMessage() {}
}
