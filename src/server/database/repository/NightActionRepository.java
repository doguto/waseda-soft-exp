package src.server.database.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.common.Role;

public class NightActionRepository {
    private final GameDatabase db = GameDatabase.getInstance();
    private final String roomId;

    public NightActionRepository(String roomId) {
        this.roomId = roomId;
    }

    public void saveWolfAttack(String wolfName, String targetName) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.wolfAttacks.put(wolfName, targetName);
    }

    public boolean allWolvesAttacked() {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        long aliveWolves = new PlayerRepository(roomId).getAlivePlayers().stream()
            .filter(p -> p.role == Role.WOLF).count();
        return room.wolfAttacks.size() >= aliveWolves;
    }

    public Optional<String> resolveAttack() {
        RoomData room = db.getRoom(roomId);
        if (room == null || room.wolfAttacks.isEmpty()) return Optional.empty();

        Map<String, String> attacks = room.wolfAttacks;

        Map<String, Long> counts = attacks.values().stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        long max = Collections.max(counts.values());
        List<String> top = counts.entrySet().stream()
            .filter(e -> e.getValue() == max)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        return Optional.of(top.get(new Random().nextInt(top.size())));
    }

    public void saveSeerTarget(String targetName) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.seerTarget = targetName;
    }

    public Optional<String> getSeerTarget() {
        RoomData room = db.getRoom(roomId);
        return room != null ? Optional.ofNullable(room.seerTarget) : Optional.empty();
    }

    public void saveKnightTarget(String targetName) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.knightTarget = targetName;
    }

    public Optional<String> getKnightTarget() {
        RoomData room = db.getRoom(roomId);
        return room != null ? Optional.ofNullable(room.knightTarget) : Optional.empty();
    }

    public Optional<String> getLastKnightTarget() {
        RoomData room = db.getRoom(roomId);
        return room != null ? Optional.ofNullable(room.lastKnightTarget) : Optional.empty();
    }

    public void updateLastKnightTarget() {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.lastKnightTarget = room.knightTarget;
    }

    public void reset() {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.resetNightActions();
    }
}
