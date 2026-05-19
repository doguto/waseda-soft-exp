package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.server.game.GameMaster;

public class EndDiscussionService extends BaseService {

    public EndDiscussionService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
        // stateManager.check(GameEvent.DISCUSSION_ENDED) を呼んでフェーズを VOTE に遷移させる
        //   → discussionEnded フラグで二重呼び出しを防止済み
        // gameMaster.pushService(ServiceType.VOTE_PHASE_START) をキューに積む
        // 成功を EndDiscussionResultMessage に設定して返す
        return new EndDiscussionResultMessage();
    }
}
