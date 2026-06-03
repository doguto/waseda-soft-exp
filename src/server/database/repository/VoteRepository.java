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
    private final String roomId;

    public record VoteResolution(Optional<String> target, Map<String, Integer> counts) {}

    public VoteRepository(String roomId) {
        this.roomId = roomId;
    }

    public void setResolvedTarget(String targetName) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.resolvedVoteTarget = targetName;
    }

    public Optional<String> getResolvedTarget() {
        RoomData room = db.getRoom(roomId);
        return room != null ? Optional.ofNullable(room.resolvedVoteTarget) : Optional.empty();
    }

    public void save(String playerName, String targetName) {
        RoomData room = db.getRoom(roomId);
        if (room != null) {
            // 同一プレイヤーの再投票は無視する
            room.votes.putIfAbsent(playerName, targetName);
        }
    }

    public boolean hasVoteFrom(String playerName) {
        RoomData room = db.getRoom(roomId);
        return room != null && room.votes.containsKey(playerName);
    }

    public boolean allVoted() {
        RoomData room = db.getRoom(roomId);
        if (room == null) return false;
        long alivePlayers = new PlayerRepository(roomId).getAlivePlayers().size();
        return room.votes.size() >= alivePlayers;
    }

    public VoteResolution resolveTarget() {
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

    public void reset() {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.resetVotes();
    }
}
