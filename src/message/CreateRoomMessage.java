package src.message;

public class CreateRoomMessage {
    public static final String MessageType = "create_room";
    public String message_type = MessageType;
    public String roomId;
    public String playerId;
    public String name;

    public CreateRoomMessage() {}
}
