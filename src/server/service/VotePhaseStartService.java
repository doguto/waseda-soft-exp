package src.server.service;

import src.message.VotePhaseStartMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.game.GameMaster;
import src.server.game.GamePhase;

public class VotePhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public VotePhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // VoteRepository.reset(roomId) で前回の投票結果をクリアする
        gameMaster.voteRepository.reset();
        // stateManager.setPhase(GamePhase.VOTE) でフェーズを投票に設定する
        stateManager.setPhase(GamePhase.VOTE);
        // broadcaster.broadcastAlive(roomId, ...) で生存者に投票開始を通知する
        broadcaster.broadcastAlive(roomId, new VotePhaseStartMessage());
    }
}
