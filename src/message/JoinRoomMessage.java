package src.message;

public class JoinRoomMessage {
    public static final String MessageType = "join_room";
    public String message_type = MessageType;
    public String roomId;
    public String playerId;
    public String name;

    public JoinRoomMessage() {}
}
