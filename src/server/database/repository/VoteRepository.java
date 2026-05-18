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

    public record VoteResolution(Optional<String> target, Map<String, Integer> counts) {}

    public void save(String roomId, String playerName, String targetName) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.votes.put(playerName, targetName);
    }

    public boolean allVoted(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        long alivePlayers = playerRepository.getAlivePlayers(roomId).size();
        return room.votes.size() >= alivePlayers;
    }

    // 最多票のプレイヤーと得票数を返す。同票の場合はランダムで決定する。
    public VoteResolution resolveTarget(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room == null || room.votes.isEmpty()) {
            return new VoteResolution(Optional.empty(), Map.of());
        }

        Map<String, String> votes = room.votes;

        Map<String, Integer> counts = votes.values().stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.summingInt(t -> 1)));
        long max = Collections.max(counts.values());
        List<String> top = counts.entrySet().stream()
            .filter(e -> e.getValue() == max)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        Optional<String> target = Optional.of(top.get(new Random().nextInt(top.size())));
        return new VoteResolution(target, counts);
    }

    public void reset(String roomId) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.resetVotes();
    }
}
