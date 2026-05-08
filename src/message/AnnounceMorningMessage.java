package src.message;

public class AnnounceMorningMessage {
    public static final String MessageType = "announce_morning";
    public String message_type = MessageType;
    public String deadPlayerName; // null if no one died (knight guarded)

    public AnnounceMorningMessage() {}
}
