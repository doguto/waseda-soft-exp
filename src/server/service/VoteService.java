package src.server.service;

import src.message.VoteMessage;
import src.message.VoteResultMessage;
import src.server.game.GameMaster;

public class VoteService extends BaseService {

    public VoteService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public VoteResultMessage call(VoteMessage msg) {
        // VoteRepository.save(roomId, msg.playerName, msg.targetName) で投票を保存する
        // stateManager.check(GameEvent.VOTE_SUBMITTED) を呼ぶ
        //   → 全生存者が投票済みなら GameStateManager が DISTRIBUTE_VOTE_RESULT をキューに積む
        //   → compareAndSet で二重キューイングを防止済み
        // 成功を VoteResultMessage に設定して返す
        return new VoteResultMessage();
    }
}
