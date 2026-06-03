package src.message;

public class ExecuteReadyMessage {
    public static final String MessageType = "execute_ready";
    public String message_type = MessageType;
    public String roomId;
    public String playerName;

    public ExecuteReadyMessage() {}
    public ExecuteReadyMessage(String roomId, String playerName) {
        this.roomId = roomId;
        this.playerName = playerName;
    }
}
