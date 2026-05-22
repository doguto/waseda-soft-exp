package src.server.database.repository;

import java.util.List;
import java.util.Optional;
import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.Player;
import src.common.Role;

public class PlayerRepository {
    private final GameDatabase db = GameDatabase.getInstance();
    private final String roomId;

    public PlayerRepository(String roomId) {
        this.roomId = roomId;
    }

    public Optional<Player> findByName(String name) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return Optional.empty();
        return room.players.stream().filter(p -> p.name.equals(name)).findFirst();
    }

    public boolean setRole(String name, Role role) {
        return findByName(name).map(p -> { p.role = role; return true; }).orElse(false);
    }

    public boolean kill(String name) {
        return findByName(name).map(p -> { p.alive = false; return true; }).orElse(false);
    }

    public List<Player> getAlivePlayers() {
        RoomData room = db.getRoom(roomId);
        if (room == null) return List.of();
        return room.players.stream().filter(p -> p.alive).toList();
    }

    // 人狼陣営勝利: 生存人狼数 >= 生存村人陣営数
    // 村人陣営勝利: 生存人狼が0
    public boolean wolvesWin() {
        List<Player> alive = getAlivePlayers();
        long wolves   = alive.stream().filter(p -> p.role == Role.WOLF).count();
        long villagers = alive.stream().filter(p -> p.role != Role.WOLF).count();
        return wolves >= villagers;
    }

    public boolean villagersWin() {
        return getAlivePlayers().stream().noneMatch(p -> p.role == Role.WOLF);
    }

    public List<String> getPlayerNames() {
        RoomData room = db.getRoom(roomId);
        if (room == null) return List.of();
        return room.players.stream().map(p -> p.name).toList();
    }

    public Role getPlayerRole(String name) {
        return findByName(name).map(p -> p.role).orElse(null);
    }

    public boolean isAlive(String name) {
        return findByName(name).map(p -> p.alive).orElse(false);
    }
}
