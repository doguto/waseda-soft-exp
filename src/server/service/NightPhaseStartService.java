package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;

public class NightPhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public NightPhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 夜アクションをリセットしてフェーズを夜に設定する
    }
}
