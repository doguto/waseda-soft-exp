package src.server.service;

import src.message.SeerInvestigateMessage;
import src.message.SeerInvestigateResultMessage;
import src.server.GameMaster;

public class SeerInvestigateService extends BaseService {

    public SeerInvestigateService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public SeerInvestigateResultMessage call(SeerInvestigateMessage msg) {
        // 占い先を保存し、全夜アクション完了なら次フェーズへ遷移する（結果は翌朝通知）
        return new SeerInvestigateResultMessage();
    }
}
