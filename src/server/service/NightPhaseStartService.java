package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class NightPhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public NightPhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // NightActionRepository.reset(roomId) で前回の夜アクションをクリアする
        // stateManager.incrementNight() で夜カウントを進める
        // stateManager.resetRoundState() で voteResolved, discussionEnded フラグをリセットする
        // stateManager.setPhase(GamePhase.NIGHT) でフェーズを夜に設定する
        // broadcaster.broadcastAlive(roomId, ...) で生存者に夜開始を通知する
    }
}
