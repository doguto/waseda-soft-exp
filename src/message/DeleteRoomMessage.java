package src.message;

public class DeleteRoomMessage {
    public static final String MessageType = "delete_room";
    public String message_type = MessageType;
    public String roomId;

    public DeleteRoomMessage() {}
}
