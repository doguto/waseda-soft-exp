package src.server.service;

import src.message.KnightGuardMessage;
import src.message.KnightGuardResultMessage;
import src.server.GameEvent;
import src.server.GameMaster;
import src.server.database.repository.NightActionRepository;

public class KnightGuardService extends BaseService {
    private final NightActionRepository nightActionRepo = new NightActionRepository();

    public KnightGuardService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public KnightGuardResultMessage call(KnightGuardMessage msg) {
        nightActionRepo.saveKnightTarget(msg.roomId, msg.targetId);
        stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED);
        return new KnightGuardResultMessage(true);
    }
}
