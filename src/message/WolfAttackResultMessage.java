package src.message;

public class WolfAttackResultMessage {
    public static final String MessageType = "wolf_attack_result";
    public String message_type = MessageType;
    public boolean success;

    public WolfAttackResultMessage() {}
    public WolfAttackResultMessage(boolean success) { this.success = success; }
}
