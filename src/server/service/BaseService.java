package src.server.service;

import src.server.database.repository.RoomRepository;
import src.server.game.GameMaster;
import src.server.game.GameStateManager;

public abstract class BaseService {
    protected final String roomId;
    protected final GameMaster gameMaster;
    protected final GameStateManager stateManager;
    protected final RoomRepository roomRepository;

    protected BaseService(String roomId, GameMaster gameMaster) {
        this.roomId = roomId;
        this.gameMaster = gameMaster;
        stateManager = gameMaster.getStateManager();
        roomRepository = null;
    }

    protected BaseService(String roomId, GameMaster gameMaster, RoomRepository roomRepository) {
        this.roomId = roomId;
        this.gameMaster = gameMaster;
        this.stateManager = gameMaster.getStateManager();
        this.roomRepository = roomRepository;
    }
}
