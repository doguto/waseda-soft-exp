package src.message;

import java.util.List;

public class AnnounceGameOverMessage {
    public static final String MessageType = "announce_game_over";
    public String message_type = MessageType;
    public String winner; // "WOLF" or "VILLAGER"
    public List<PlayerResult> players;

    public AnnounceGameOverMessage() {}
    public AnnounceGameOverMessage(String winner, List<PlayerResult> players) {
        this.winner = winner;
        this.players = players;
    }

    public static class PlayerResult {
        public String id;
        public String name;
        public String role;

        public PlayerResult() {}
        public PlayerResult(String id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }
    }
}
