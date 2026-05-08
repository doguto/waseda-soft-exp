package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;

public class AnnounceGameOverService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public AnnounceGameOverService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 勝利陣営と全プレイヤーの役職を含むゲーム終了メッセージをブロードキャストする
    }
}
