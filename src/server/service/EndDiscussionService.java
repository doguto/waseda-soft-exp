package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.server.game.GameEvent;
import src.server.game.GameMaster;

public class EndDiscussionService extends BaseService {

    public EndDiscussionService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
        // GameStateManager.check() 内の compareAndSet が二重発火を防止する
        // VOTE_PHASE_START のキュー投入も check() 内で行われる
        stateManager.check(GameEvent.DISCUSSION_ENDED);
        return new EndDiscussionResultMessage(true);
    }
}
