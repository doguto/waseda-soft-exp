package src.message;

public class CreateRoomResultMessage {
    public static final String MessageType = "create_room_result";
    public String message_type = MessageType;
    public boolean success;
    public String message;

    public CreateRoomResultMessage() {}
    public CreateRoomResultMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
