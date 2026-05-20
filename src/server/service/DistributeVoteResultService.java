package src.server.service;

import src.message.DistributeVoteResultMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.database.repository.VoteRepository.VoteResolution;
import src.server.game.GameMaster;

public class DistributeVoteResultService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public DistributeVoteResultService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // VoteRepository.resolveTarget(roomId) で最多票プレイヤー (同票ならランダム) と集計結果を取得する
        VoteResolution resolution = gameMaster.voteRepository.resolveTarget();
        String targetName = resolution.target().orElse(null);

        // 各プレイヤーの得票数をまとめた結果をルーム全体へ通知する
        broadcaster.broadcast(roomId, new DistributeVoteResultMessage(targetName, resolution.counts()));
        // gameMaster.pushService(ServiceType.EXECUTE) で処刑サービスをキューに積む
        gameMaster.pushService(ServiceType.EXECUTE);
    }
}
