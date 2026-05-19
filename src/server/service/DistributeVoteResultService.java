package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class DistributeVoteResultService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public DistributeVoteResultService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // VoteRepository.resolveTarget(roomId) で最多票プレイヤー (同票ならランダム) を取得する
        // 各プレイヤーの得票数をまとめた結果を broadcaster.broadcastAlive(roomId, ...) で通知する
        // gameMaster.pushService(ServiceType.EXECUTE) で処刑サービスをキューに積む
    }
}
