package src.server.database;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import src.server.database.entity.ChatMessage;
import src.server.database.entity.Player;

public class RoomData {
    public final List<Player>        players      = new CopyOnWriteArrayList<>();
    public final Map<String, String> votes        = new ConcurrentHashMap<>(); // playerName -> targetName
    public final Map<String, String> wolfAttacks  = new ConcurrentHashMap<>(); // wolfName -> targetName
    public volatile String           knightTarget     = null;
    public volatile String           lastKnightTarget = null;
    public volatile String           seerTarget       = null;
    public volatile String           executedPlayerName = null;
    public volatile String           resolvedVoteTarget = null;

    public final List<ChatMessage> villageChat = new CopyOnWriteArrayList<>();
    public final List<ChatMessage> wolfChat    = new CopyOnWriteArrayList<>();
    public final List<ChatMessage> graveChat   = new CopyOnWriteArrayList<>();

    public void resetNightActions() {
        wolfAttacks.clear();
        knightTarget = null;
        seerTarget = null;
    }

    public void resetVotes() {
        votes.clear();
        resolvedVoteTarget = null;
    }
}
