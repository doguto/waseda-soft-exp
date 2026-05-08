package src.server.service;

import src.message.WolfAttackMessage;
import src.message.WolfAttackResultMessage;
import src.server.GameMaster;

public class WolfAttackService extends BaseService {

    public WolfAttackService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public WolfAttackResultMessage call(WolfAttackMessage msg) {
        // 狼の攻撃先を保存し、全夜アクション完了なら次フェーズへ遷移する
        return new WolfAttackResultMessage();
    }
}
