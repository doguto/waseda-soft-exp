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
        // VoteRepository.reset(roomId) で前回の投票結果をクリアする
        // stateManager.setPhase(GamePhase.VOTE) でフェーズを投票に設定する
        // broadcaster.broadcastAlive(roomId, ...) で生存者に投票開始を通知する
    }
}
