package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class ExecuteService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public ExecuteService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // VoteRepository.resolveTarget(roomId) で処刑対象を取得する
        // PlayerRepository.kill(roomId, targetName) で処刑する
        // RoomData.executedPlayerName に処刑したプレイヤー名を保存する（霊媒師が翌朝の AnnounceMorningService で使用）
        // 処刑者の名前とロールを broadcaster.broadcastAlive(roomId, ...) で全体通知する
        // PlayerRepository.wolvesWin / villagersWin で勝利判定を行う
        //   → 勝利なら gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER) をキューに積む
        //   → 続行なら gameMaster.pushService(ServiceType.NIGHT_PHASE_START) をキューに積む
    }
}
