package src.server.service;

import src.message.SeerInvestigateMessage;
import src.message.SeerInvestigateResultMessage;
import src.server.game.GameMaster;

public class SeerInvestigateService extends BaseService {

    public SeerInvestigateService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public SeerInvestigateResultMessage call(SeerInvestigateMessage msg) {
        // NightActionRepository.saveSeerTarget(roomId, msg.targetName) で占い先を保存する
        // stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED) を呼ぶ
        //   → 全夜アクション完了なら GameStateManager が ANNOUNCE_MORNING をキューに積む
        // 占い結果 (対象プレイヤーのロール) は AnnounceMorningService で占い師にユニキャストされる
        // 成功を SeerInvestigateResultMessage に設定して返す
        return new SeerInvestigateResultMessage();
    }
}
