package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class VotePhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public VotePhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // フェーズを投票に設定する
    }
}
