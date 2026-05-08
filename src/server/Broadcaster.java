package src.server;

// Implemented by JabberServer. Broadcast services use this to send messages without touching sockets directly.
public interface Broadcaster {
    void broadcast(String roomId, Object message);
    void sendTo(String playerId, Object message);
    void broadcastAlive(String roomId, Object message);
    void broadcastToRole(String roomId, src.server.database.entity.Role role, Object message);
    void broadcastDead(String roomId, Object message);
}
