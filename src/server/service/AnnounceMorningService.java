package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class AnnounceMorningService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public AnnounceMorningService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // NightActionRepository.resolveAttack(roomId) で狼の攻撃対象 (最多票・同票ならランダム) を取得する
        // NightActionRepository.getKnightTarget(roomId) で騎士の護衛対象を取得する
        // 攻撃対象 ≠ 護衛対象であれば PlayerRepository.kill(roomId, attackedName) で死亡処理をする
        // 死亡者名 (または「犠牲者なし」) を broadcaster.broadcastAlive(roomId, ...) で全体通知する
        // NightActionRepository.getSeerTarget(roomId) で占い先を取得し、対象プレイヤーのロールを占い師にユニキャストする
        // PlayerRepository.wolvesWin / villagersWin で勝利判定を行う
        //   → 勝利なら gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER) をキューに積む
        //   → 続行なら stateManager.setPhase(GamePhase.DISCUSSION) でフェーズを昼議論に設定する
    }
}
