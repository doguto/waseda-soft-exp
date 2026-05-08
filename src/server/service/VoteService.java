package src.server.service;

import src.message.VoteMessage;
import src.message.VoteResultMessage;
import src.server.GameMaster;

public class VoteService extends BaseService {

    public VoteService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public VoteResultMessage call(VoteMessage msg) {
        // 投票を保存し、全員投票済みなら次フェーズへ遷移する
        return new VoteResultMessage();
    }
}
