package src.server.database.repository;

import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.Player;

import java.util.List;

public class RoomRepository {
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

    public List<Player> getPlayers(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return List.of();
        return room.players;
    }

    public boolean canStart(String roomId) {
        return getPlayers(roomId).size() >= 4;
    }
}
