package src.server.service;

import src.server.game.GameMaster;
import src.server.game.GameStateManager;

public abstract class BaseService {
    protected final String roomId;
    protected final GameMaster gameMaster;
    protected final GameStateManager stateManager;

    protected BaseService(String roomId, GameMaster gameMaster) {
        this.roomId = roomId;
        this.gameMaster = gameMaster;
        this.stateManager = gameMaster.getStateManager();
    }
}
