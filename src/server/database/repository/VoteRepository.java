package src.server.database.repository;

import src.server.database.GameDatabase;
import src.server.database.RoomData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class VoteRepository {
    private final GameDatabase db = GameDatabase.getInstance();
    private final PlayerRepository playerRepository = new PlayerRepository();

    public void save(String roomId, String playerId, String targetId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.votes.put(playerId, targetId);
    }

    public boolean allVoted(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        long alivePlayers = playerRepository.getAlivePlayers(roomId).size();
        return room.votes.size() >= alivePlayers;
    }

    // 最多票のプレイヤーを返す。同票の場合はランダムで決定する。
    public Optional<String> resolveTarget(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null || room.votes.isEmpty()) return Optional.empty();

        Map<String, String> votes = room.votes;

        Map<String, Long> counts = votes.values().stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        long max = Collections.max(counts.values());
        List<String> top = counts.entrySet().stream()
            .filter(e -> e.getValue() == max)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        return Optional.of(top.get(new Random().nextInt(top.size())));
    }

    public void reset(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.resetVotes();
    }
}
