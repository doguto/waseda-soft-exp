package src.server.service;

import src.message.EndDiscussionMessage;
import src.message.EndDiscussionResultMessage;
import src.message.EndDiscussionStatusMessage;
import src.server.database.repository.RoomRepository;
import src.server.core.Broadcaster;
import src.server.game.GameEvent;
import src.server.game.GameMaster;

public class EndDiscussionService extends BaseService {

    private final RoomRepository repo = new RoomRepository();
    private final Broadcaster broadcaster;

    public EndDiscussionService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
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

        // 常に現在の賛成数をルーム内へ通知
        try {
            broadcaster.broadcast(roomId, new EndDiscussionStatusMessage(votesFor, alive, need));
        } catch (Exception e) {
            // ignore
        }

        if (votesFor >= need) {
            // 過半数を満たした → フェーズ遷移
            stateManager.check(GameEvent.DISCUSSION_ENDED);
            // クリア
            repo.clearEndDiscussionRequests(roomId);
            // 最終状態も通知（全員賛成扱い）
            try {
                broadcaster.broadcast(roomId, new EndDiscussionStatusMessage(votesFor, alive, need));
            } catch (Exception e) {}
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
