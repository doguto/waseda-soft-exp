package src.message;

public class DeleteRoomResultMessage {
    public static final String MessageType = "delete_room_result";
    public String message_type = MessageType;
    public boolean success;

    public DeleteRoomResultMessage() {}
    public DeleteRoomResultMessage(boolean success) { this.success = success; }
}
