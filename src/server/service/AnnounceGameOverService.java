package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class AnnounceGameOverService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public AnnounceGameOverService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // PlayerRepository.wolvesWin(roomId) で勝利陣営 (WOLF / VILLAGE) を判定する
        // RoomRepository.getPlayers(roomId) で全プレイヤーの名前とロールを取得する
        // 勝利陣営と全プレイヤーのロール一覧を含むメッセージを broadcaster.broadcastAlive(roomId, ...) で通知する
    }
}
