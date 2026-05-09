package src.server.service;

import src.message.KnightGuardMessage;
import src.message.KnightGuardResultMessage;
import src.server.game.GameMaster;

public class KnightGuardService extends BaseService {

    public KnightGuardService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public KnightGuardResultMessage call(KnightGuardMessage msg) {
        // NightActionRepository.saveKnightTarget(roomId, msg.targetName) で護衛先を保存する
        // stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED) を呼ぶ
        //   → 全夜アクション完了なら GameStateManager が ANNOUNCE_MORNING をキューに積む
        // 成功を KnightGuardResultMessage に設定して返す
        return new KnightGuardResultMessage();
    }
}
