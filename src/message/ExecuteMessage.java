package src.message;

public class ExecuteMessage {
    public static final String MessageType = "execute";
    public String message_type = MessageType;
    public String executedPlayerName;
    public String executedRole;

    public ExecuteMessage() {}
    public ExecuteMessage(String executedPlayerName, String executedRole) {
        this.executedPlayerName = executedPlayerName;
        this.executedRole = executedRole;
    }
}
