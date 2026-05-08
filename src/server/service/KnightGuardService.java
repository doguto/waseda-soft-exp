package src.server.service;

import src.message.KnightGuardMessage;
import src.message.KnightGuardResultMessage;
import src.server.GameMaster;

public class KnightGuardService extends BaseService {

    public KnightGuardService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public KnightGuardResultMessage call(KnightGuardMessage msg) {
        // 護衛先を保存し、全夜アクション完了なら次フェーズへ遷移する
        return new KnightGuardResultMessage();
    }
}
