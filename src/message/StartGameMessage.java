package src.message;

public class StartGameMessage {
    public static final String MessageType = "start_game";
    public String message_type = MessageType;
    public String roomId;

    public StartGameMessage() {}
}
