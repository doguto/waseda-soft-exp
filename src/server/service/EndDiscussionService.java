package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.server.database.repository.RoomRepository;
import src.server.game.GameEvent;
import src.server.game.GameMaster;

public class EndDiscussionService extends BaseService {

    private final RoomRepository repo = new RoomRepository();

    public EndDiscussionService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    /**
     * Request to end discussion from a player. Advances phase only when majority of alive players have requested.
     */
    public EndDiscussionResultMessage call(EndDiscussionMessage msg, String requesterName) {
        // 登録
        boolean added = repo.addEndDiscussionRequest(roomId, requesterName);

        int votesFor = repo.countEndDiscussionRequests(roomId);
        int alive = gameMaster.playerRepository.getAlivePlayers().size();
        int need = (alive / 2) + 1; // 過半数

        if (votesFor >= need) {
            // 過半数を満たした → フェーズ遷移
            stateManager.check(GameEvent.DISCUSSION_ENDED);
            // クリア
            repo.clearEndDiscussionRequests(roomId);
            return new EndDiscussionResultMessage(true);
        } else {
            // 過半数に達していない
            return new EndDiscussionResultMessage(false);
        }
    }

    // 既存の呼び出しシグネチャとの互換性のためのオーバーロード: もし requester が渡されなければ即時チェックしない
    public EndDiscussionResultMessage call(EndDiscussionMessage msg) {
        // 互換性維持：リクエスト元が不明な場合は従来どおり即時成功させない（false を返す）
        return new EndDiscussionResultMessage(false);
    }
}
