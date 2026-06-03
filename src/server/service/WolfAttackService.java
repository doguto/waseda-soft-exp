package src.server.service;

import src.message.WolfAttackMessage;
import src.message.WolfAttackResultMessage;
import src.server.game.GameEvent;
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

        if (gameMaster.nightActionRepository.hasWolfAttacked(msg.wolfName)) {
            return new WolfAttackResultMessage(false, "すでに襲撃を選択済みです。");
        }
        gameMaster.nightActionRepository.saveWolfAttack(msg.wolfName, msg.targetName);

        // 夜行動が提出されたことを通知
        gameMaster.getStateManager().check(GameEvent.NIGHT_ACTION_SUBMITTED);

        // 成功を返す（success=true を明示する）
        return new WolfAttackResultMessage(true);
    }
}
