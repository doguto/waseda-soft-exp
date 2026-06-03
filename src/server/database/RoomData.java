package src.server.database;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.CopyOnWriteArrayList;
import src.server.database.entity.ChatMessage;
import src.server.database.entity.Player;

public class RoomData {
    public final List<Player>        players      = new CopyOnWriteArrayList<>();
    // ルーム作成者（ホスト）の名前
    public volatile String           hostName      = null;
    public final Map<String, String> votes        = new ConcurrentHashMap<>(); // playerName -> targetName
    public final Map<String, String> wolfAttacks  = new ConcurrentHashMap<>(); // wolfName -> targetName
    public volatile String           knightTarget     = null;
    public volatile String           lastKnightTarget = null;
    public volatile String           seerTarget       = null;
    public volatile String           executedPlayerName = null;
    public final KeySetView<String, Boolean> endDiscussionRequests = ConcurrentHashMap.newKeySet();
    public final KeySetView<String, Boolean> executeReadyPlayers   = ConcurrentHashMap.newKeySet();
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
        endDiscussionRequests.clear();
    }
}
