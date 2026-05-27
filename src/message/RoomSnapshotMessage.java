package src.message;

import java.util.List;

public class RoomSnapshotMessage {
    public static final String MessageType = "room_snapshot";
    public String message_type = MessageType;

    public List<String> players;
    public List<String> deadPlayers;
    public String myRole; // role name or null
    public boolean isAlive;
    public String phase;
    public int endDiscussionFor;
    public int endDiscussionNeed;
    public int endDiscussionAlive;
    public boolean hasVoted;
    public boolean hasNightActionSent;
    public boolean rolesAssigned;

    public List<String> villageChat;
    public List<String> wolfChat;
    public List<String> graveChat;

    public RoomSnapshotMessage() {}
}
