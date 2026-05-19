package src.server.core;

// Marker interface for services called by Worker (server-initiated broadcasts).
public interface BroadcastService {
    void call();
}
