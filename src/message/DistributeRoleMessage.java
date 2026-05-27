package src.message;

import java.util.List;

public class DistributeRoleMessage {
    public static final String MessageType = "distribute_role";
    public String message_type = MessageType;
    public String role;
    public List<String> player_names;

    public DistributeRoleMessage() {}
    public DistributeRoleMessage(String role, List<String> player_names) {
        this.role = role;
        this.player_names = player_names;
    }
}
