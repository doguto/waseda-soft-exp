package src.server.service;

import src.message.SeerInvestigateMessage;
import src.message.SeerInvestigateResultMessage;
import src.server.GameEvent;
import src.server.GameMaster;
import src.server.database.repository.NightActionRepository;

public class SeerInvestigateService extends BaseService {
    private final NightActionRepository nightActionRepo = new NightActionRepository();

    public SeerInvestigateService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public SeerInvestigateResultMessage call(SeerInvestigateMessage msg) {
        nightActionRepo.saveSeerTarget(msg.roomId, msg.targetName);
        stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED);
        // 占い結果は AnnounceMorningService で翌朝に通知する
        return new SeerInvestigateResultMessage(true);
    }
}
