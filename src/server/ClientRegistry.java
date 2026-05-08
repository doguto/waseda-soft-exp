package src.server;

import src.server.database.entity.Player;
import src.server.database.entity.Role;
import src.server.database.repository.PlayerRepository;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    private final Map<String, PrintWriter> clients     = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomPlayers = new ConcurrentHashMap<>();
    private final PlayerRepository playerRepo = new PlayerRepository();

    public void register(String playerId, PrintWriter writer) {
        clients.put(playerId, writer);
    }

    public void joinRoom(String roomId, String playerId) {
        roomPlayers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
    }

    public void remove(String playerId) {
        clients.remove(playerId);
        roomPlayers.values().forEach(s -> s.remove(playerId));
    }

    public void sendTo(String playerId, String json) {
        PrintWriter w = clients.get(playerId);
        if (w != null) w.println(json);
    }

    public void broadcastToRoom(String roomId, String json) {
        Set<String> players = roomPlayers.getOrDefault(roomId, Set.of());
        players.forEach(pid -> sendTo(pid, json));
    }

    public void broadcastAlive(String roomId, String json) {
        playerRepo.getAlivePlayers(roomId).forEach(p -> sendTo(p.id, json));
    }

    public void broadcastToRole(String roomId, Role role, String json) {
        playerRepo.getAlivePlayers(roomId).stream()
            .filter(p -> p.role == role)
            .forEach(p -> sendTo(p.id, json));
    }

    public void broadcastDead(String roomId, String json) {
        Set<String> all = roomPlayers.getOrDefault(roomId, Set.of());
        all.stream()
            .filter(pid -> playerRepo.findById(roomId, pid).map(p -> !p.alive).orElse(false))
            .forEach(pid -> sendTo(pid, json));
    }
}
