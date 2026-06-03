package src.message;

public class WolfAttackResultMessage {
    public static final String MessageType = "wolf_attack_result";
    public String message_type = MessageType;
    public boolean success;
    public String message;

    public WolfAttackResultMessage() {}
    public WolfAttackResultMessage(boolean success) { this.success = success; }
    public WolfAttackResultMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
