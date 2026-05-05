package src.server.database.repository;

import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.Role;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class NightActionRepository {
    private final GameDatabase db = GameDatabase.getInstance();
    private final PlayerRepository playerRepository = new PlayerRepository();

    // 人狼の投票
    public void saveWolfAttack(String roomId, String wolfId, String targetId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.wolfAttacks.put(wolfId, targetId);
    }

    public boolean allWolvesAttacked(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        long aliveWolves = playerRepository.getAlivePlayers(roomId).stream()
            .filter(p -> p.role == Role.WOLF).count();
        return room.wolfAttacks.size() >= aliveWolves;
    }

    // 最多票の襲撃対象を返す。同票の場合はランダムで決定する。
    public Optional<String> resolveAttack(String roomId) {
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

    // 占い師
    public void saveSeerTarget(String roomId, String targetId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.seerTarget = targetId;
    }

    public Optional<String> getSeerTarget(String roomId) {
        RoomData room = db.getRoom(roomId);
        return room != null ? Optional.ofNullable(room.seerTarget) : Optional.empty();
    }

    // 騎士
    public void saveKnightTarget(String roomId, String targetId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.knightTarget = targetId;
    }

    public Optional<String> getKnightTarget(String roomId) {
        RoomData room = db.getRoom(roomId);
        return room != null ? Optional.ofNullable(room.knightTarget) : Optional.empty();
    }

    public void reset(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.resetNightActions();
    }
}
