package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.server.GameEvent;
import src.server.GameMaster;

public class EndDiscussionService extends BaseService {
    public EndDiscussionService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
        stateManager.check(GameEvent.DISCUSSION_ENDED);
        return new EndDiscussionResultMessage(true);
    }
}
