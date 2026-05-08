package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.GamePhase;

public class VotePhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public VotePhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        stateManager.setPhase(GamePhase.VOTE);
    }
}
