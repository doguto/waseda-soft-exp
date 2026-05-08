package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;

public class ExecuteService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public ExecuteService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 最多票プレイヤーを処刑してブロードキャスト、勝利判定後に次フェーズサービスをキューに積む
    }
}
