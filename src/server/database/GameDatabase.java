package src.server.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameDatabase {
    private static final GameDatabase INSTANCE = new GameDatabase();

    private final Map<String, RoomData> rooms = new ConcurrentHashMap<>();

    private GameDatabase() {}

    public static GameDatabase getInstance() {
        return INSTANCE;
    }

    public boolean createRoom(String roomId) {
        return rooms.putIfAbsent(roomId, new RoomData()) == null;
    }

    public boolean deleteRoom(String roomId) {
        return rooms.remove(roomId) != null;
    }

    public RoomData getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }
}
