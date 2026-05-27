package src.server.service;

import src.message.NightPhaseStartMessage;
import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;
import src.common.GamePhase;

public class NightPhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public NightPhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        gameMaster.nightActionRepository.reset();
        // centralize phase transition
        stateManager.beginNight();
        broadcaster.broadcast(roomId, new NightPhaseStartMessage());
    }
}
