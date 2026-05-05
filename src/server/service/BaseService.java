package src.server.service;

import src.server.GameStateManager;

public abstract class BaseService {
    protected final String roomId;
    protected final GameStateManager stateManager;

    protected BaseService(String roomId, GameStateManager stateManager) {
        this.roomId = roomId;
        this.stateManager = stateManager;
    }
}
