package src.message;

import java.util.List;

public class StartGameResultMessage {
    public static final String MessageType = "start_game_result";
    public String message_type = MessageType;
    public boolean success;
    public String message;
    public List<String> player_names;

    public StartGameResultMessage() {}
    public StartGameResultMessage(boolean success, String message, List<String> player_names) {
        this.success = success;
        this.message = message;
        this.player_names = player_names;
    }
}
