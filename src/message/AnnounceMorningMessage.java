package src.message;

public class AnnounceMorningMessage {
    public static final String MessageType = "announce_morning";
    public String message_type = MessageType;
    public String deadPlayerId;   // null if no one died (knight guarded)
    public String deadPlayerName; // null if no one died
    public String seerTargetId;   // the player seer investigated (null if seer is dead)
    public Boolean seerResultIsWolf; // null if seer is dead

    public AnnounceMorningMessage() {}
}
