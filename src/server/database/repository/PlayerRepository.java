package src.server.database.repository;

import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.Player;
import src.server.database.entity.Role;

import java.util.List;
import java.util.Optional;

public class PlayerRepository {
    private final GameDatabase db = GameDatabase.getInstance();

    public Optional<Player> findByName(String roomId, String name) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return Optional.empty();
        return room.players.stream().filter(p -> p.name.equals(name)).findFirst();
    }

    public boolean setRole(String roomId, String name, Role role) {
        return findByName(roomId, name).map(p -> { p.role = role; return true; }).orElse(false);
    }

    public boolean kill(String roomId, String name) {
        return findByName(roomId, name).map(p -> { p.alive = false; return true; }).orElse(false);
    }

    public List<Player> getAlivePlayers(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return List.of();
        return room.players.stream().filter(p -> p.alive).toList();
    }

    // 人狼陣営勝利: 生存人狼数 >= 生存村人陣営数
    // 村人陣営勝利: 生存人狼が0
    public boolean wolvesWin(String roomId) {
        List<Player> alive = getAlivePlayers(roomId);
        long wolves   = alive.stream().filter(p -> p.role == Role.WOLF).count();
        long villagers = alive.stream().filter(p -> p.role != Role.WOLF).count();
        return wolves >= villagers;
    }

    public boolean villagersWin(String roomId) {
        return getAlivePlayers(roomId).stream().noneMatch(p -> p.role == Role.WOLF);
    }
}
