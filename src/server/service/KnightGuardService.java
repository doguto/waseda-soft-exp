package src.server.service;

import src.message.KnightGuardMessage;
import src.message.KnightGuardResultMessage;
import src.server.game.GameEvent;
import src.server.game.GameMaster;

public class KnightGuardService extends BaseService {
    public KnightGuardService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public KnightGuardResultMessage call(KnightGuardMessage msg) {
        // 自己護衛禁止
        if (msg.knightName.equals(msg.targetName)) {
            return new KnightGuardResultMessage(false);
        }
        // 連続護衛禁止
        if (gameMaster.nightActionRepository.getLastKnightTarget()
                .map(last -> last.equals(msg.targetName)).orElse(false)) {
            return new KnightGuardResultMessage(false);
        }

        gameMaster.nightActionRepository.saveKnightTarget(msg.targetName);
        gameMaster.getStateManager().check(GameEvent.NIGHT_ACTION_SUBMITTED);
        return new KnightGuardResultMessage(true);
    }
}