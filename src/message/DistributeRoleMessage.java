package src.message;

public class DistributeRoleMessage {
    public static final String MessageType = "distribute_role";
    public String message_type = MessageType;
    public String role;

    public DistributeRoleMessage() {}
    public DistributeRoleMessage(String role) { this.role = role; }
}
