package src.server.core;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import src.common.Role;
import src.server.database.repository.PlayerRepository;

public class ClientRegistry {
    private final Map<String, PrintWriter> clients     = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomPlayers = new ConcurrentHashMap<>();

    public void register(String playerName, PrintWriter writer) {
        clients.put(playerName, writer);
    }

    public boolean isConnected(String playerName) {
        return clients.containsKey(playerName);
    }

    public void joinRoom(String roomId, String playerName) {
        roomPlayers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(playerName);
    }

    public Set<String> getRoomPlayers(String roomId) {
        return Set.copyOf(roomPlayers.getOrDefault(roomId, Set.of()));
    }

    public String findRoomOfPlayer(String playerName) {
        for (Map.Entry<String, Set<String>> e : roomPlayers.entrySet()) {
            if (e.getValue().contains(playerName)) return e.getKey();
        }
        return null;
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
        new PlayerRepository(roomId).getAlivePlayers().forEach(p -> sendTo(p.name, json));
    }

    public void broadcastToRole(String roomId, Role role, String json) {
        new PlayerRepository(roomId).getAlivePlayers().stream()
            .filter(p -> p.role == role)
            .forEach(p -> sendTo(p.name, json));
    }

    public void broadcastDead(String roomId, String json) {
        Set<String> all = roomPlayers.getOrDefault(roomId, Set.of());
        PlayerRepository repo = new PlayerRepository(roomId);
        all.stream()
            .filter(name -> repo.findByName(name).map(p -> !p.alive).orElse(false))
            .forEach(name -> sendTo(name, json));
    }
}
