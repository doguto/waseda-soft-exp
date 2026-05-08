package src.server.service;

import src.message.WolfAttackMessage;
import src.message.WolfAttackResultMessage;
import src.server.GameEvent;
import src.server.GameMaster;
import src.server.database.repository.NightActionRepository;

public class WolfAttackService extends BaseService {
    private final NightActionRepository nightActionRepo = new NightActionRepository();

    public WolfAttackService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public WolfAttackResultMessage call(WolfAttackMessage msg) {
        nightActionRepo.saveWolfAttack(msg.roomId, msg.wolfId, msg.targetId);
        stateManager.check(GameEvent.NIGHT_ACTION_SUBMITTED);
        return new WolfAttackResultMessage(true);
    }
}
