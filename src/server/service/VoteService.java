package src.server.service;

import src.message.VoteMessage;
import src.message.VoteResultMessage;
import src.server.GameEvent;
import src.server.GameMaster;
import src.server.database.repository.VoteRepository;

public class VoteService extends BaseService {
    private final VoteRepository voteRepo = new VoteRepository();

    public VoteService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public VoteResultMessage call(VoteMessage msg) {
        voteRepo.save(msg.roomId, msg.playerName, msg.targetName);
        stateManager.check(GameEvent.VOTE_SUBMITTED);
        return new VoteResultMessage(true);
    }
}
