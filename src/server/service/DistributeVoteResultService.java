package src.server.service;

import src.message.DistributeVoteResultMessage;
import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.ServiceType;
import src.server.database.entity.Player;
import src.server.database.repository.PlayerRepository;
import src.server.database.repository.VoteRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DistributeVoteResultService extends BaseService implements BroadcastService {
    private final VoteRepository voteRepo = new VoteRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public DistributeVoteResultService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        Optional<String> targetName = voteRepo.resolveTarget(roomId);
        if (targetName.isEmpty()) return;

        Map<String, Integer> voteCounts = buildVoteCounts();
        broadcaster.broadcast(roomId, new DistributeVoteResultMessage(targetName.get(), voteCounts));

        gameMaster.pushService(ServiceType.EXECUTE);
    }

    private Map<String, Integer> buildVoteCounts() {
        Map<String, Integer> counts = new HashMap<>();
        playerRepo.getAlivePlayers(roomId).forEach(p -> counts.put(p.name, 0));
        return counts;
    }
}
