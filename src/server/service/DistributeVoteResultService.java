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
        // 投票を集計して最多票プレイヤーと票数をブロードキャストし、処刑サービスをキューに積む
    }
}
