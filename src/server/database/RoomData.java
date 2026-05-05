package src.server.database;

import src.server.database.entity.ChatMessage;
import src.server.database.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomData {
    public final List<Player>        players      = new CopyOnWriteArrayList<>();
    public final Map<String, String> votes        = new ConcurrentHashMap<>(); // playerId -> targetId
    public final Map<String, String> wolfAttacks  = new ConcurrentHashMap<>(); // wolfId -> targetId
    public volatile String           knightTarget = null;
    public volatile String           seerTarget   = null;
    public volatile String           executedPlayerId = null; // 霊媒師通知用

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
    }
}
