package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.GamePhase;
import src.server.database.repository.NightActionRepository;

public class NightPhaseStartService extends BaseService implements BroadcastService {
    private final NightActionRepository nightActionRepo = new NightActionRepository();
    private final Broadcaster broadcaster;

    public NightPhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        nightActionRepo.reset(roomId);
        stateManager.setPhase(GamePhase.NIGHT);
    }
}
