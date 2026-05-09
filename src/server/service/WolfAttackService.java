package src.server.service;

import src.message.WolfAttackMessage;
import src.message.WolfAttackResultMessage;
import src.server.game.GameMaster;

public class WolfAttackService extends BaseService {

    public WolfAttackService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public WolfAttackResultMessage call(WolfAttackMessage msg) {
        // NightActionRepository.saveWolfAttack(roomId, msg.wolfName, msg.targetName) で攻撃先を保存する
        // stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED) を呼ぶ
        //   → 全夜アクション完了なら GameStateManager が ANNOUNCE_MORNING をキューに積む
        // 成功を WolfAttackResultMessage に設定して返す
        return new WolfAttackResultMessage();
    }
}
