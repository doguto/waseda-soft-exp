package src.message;

public class JoinRoomResultMessage {
    public static final String MessageType = "join_room_result";
    public String message_type = MessageType;
    public boolean success;
    public String message;

    public JoinRoomResultMessage() {}
    public JoinRoomResultMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
