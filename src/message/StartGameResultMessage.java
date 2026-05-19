package src.message;

public class StartGameResultMessage {
    public static final String MessageType = "start_game_result";
    public String message_type = MessageType;
    public boolean success;
    public String message;

    public StartGameResultMessage() {}
    public StartGameResultMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
