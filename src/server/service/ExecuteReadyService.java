package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.game.GameMaster;
import src.common.GamePhase;

public class ExecuteReadyService extends BaseService {
    private final Broadcaster broadcaster;

    public ExecuteReadyService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public void call(String playerName) {
        if (stateManager.getCurrentPhase() != GamePhase.EXECUTE) return;

        RoomData room = GameDatabase.getInstance().getRoom(roomId);
        if (room == null) return;

        room.executeReadyPlayers.add(playerName);

        if (gameMaster.allExecuteReady(room) && stateManager.markNightTriggered()) {
            room.executeReadyPlayers.clear();
            gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
        }
    }
}
