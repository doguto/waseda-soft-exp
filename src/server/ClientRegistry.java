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

    public void register(String playerName, PrintWriter writer) {
        clients.put(playerName, writer);
    }

    public void joinRoom(String roomId, String playerName) {
        roomPlayers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(playerName);
    }

    public void remove(String playerName) {
        clients.remove(playerName);
        roomPlayers.values().forEach(s -> s.remove(playerName));
    }

    public void sendTo(String playerName, String json) {
        PrintWriter w = clients.get(playerName);
        if (w != null) w.println(json);
    }

    public void broadcastToRoom(String roomId, String json) {
        Set<String> players = roomPlayers.getOrDefault(roomId, Set.of());
        players.forEach(name -> sendTo(name, json));
    }

    public void broadcastAlive(String roomId, String json) {
        playerRepo.getAlivePlayers(roomId).forEach(p -> sendTo(p.name, json));
    }

    public void broadcastToRole(String roomId, Role role, String json) {
        playerRepo.getAlivePlayers(roomId).stream()
            .filter(p -> p.role == role)
            .forEach(p -> sendTo(p.name, json));
    }

    public void broadcastDead(String roomId, String json) {
        Set<String> all = roomPlayers.getOrDefault(roomId, Set.of());
        all.stream()
            .filter(name -> playerRepo.findByName(roomId, name).map(p -> !p.alive).orElse(false))
            .forEach(name -> sendTo(name, json));
    }
}
