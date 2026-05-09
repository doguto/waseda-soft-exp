package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.server.game.GameMaster;

public class EndDiscussionService extends BaseService {

    public EndDiscussionService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
        // 議論終了イベントを発火して投票フェーズへ遷移する
        return new EndDiscussionResultMessage();
    }
}
