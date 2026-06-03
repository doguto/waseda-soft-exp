package src.server.database.repository;

import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.Player;

import java.util.List;

public class RoomRepository {
    /** ゲーム開始に必要な最小人数。 */
    public static final int MIN_PLAYERS = 4;
    /** 1ルームの最大人数（役職構成の上限）。 */
    public static final int MAX_PLAYERS = 12;

    private final GameDatabase db = GameDatabase.getInstance();

    public boolean create(String roomId) {
        return db.createRoom(roomId);
    }

    public boolean delete(String roomId) {
        return db.deleteRoom(roomId);
    }

    public boolean exists(String roomId) {
        return db.roomExists(roomId);
    }

    public boolean addPlayer(String roomId, Player player) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        room.players.add(player);
        return true;
    }
    public boolean removePlayer(String roomId, String playerName) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        return room.players.removeIf(player -> player.name.equals(playerName));
    }
    public void setHost(String roomId, String hostName) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return;
        room.hostName = hostName;
    }

    public String getHost(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return null;
        return room.hostName;
    }

    public List<Player> getPlayers(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return List.of();
        return room.players;
    }

    public boolean canStart(String roomId) {
        int n = getPlayers(roomId).size();
        return n >= MIN_PLAYERS && n <= MAX_PLAYERS;
    }

    public boolean isFull(String roomId) {
        return getPlayers(roomId).size() >= MAX_PLAYERS;
    }

    public boolean addEndDiscussionRequest(String roomId, String playerName) {
        var room = db.getRoom(roomId);
        if (room == null) return false;
        return room.endDiscussionRequests.add(playerName);
    }

    public int countEndDiscussionRequests(String roomId) {
        var room = db.getRoom(roomId);
        if (room == null) return 0;
        return room.endDiscussionRequests.size();
    }

    public void clearEndDiscussionRequests(String roomId) {
        var room = db.getRoom(roomId);
        if (room == null) return;
        room.endDiscussionRequests.clear();
    }
}
