package src.server.service;

import src.message.ExecuteMessage;
import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.GamePhase;
import src.server.ServiceType;
import src.server.database.entity.Player;
import src.server.database.repository.PlayerRepository;
import src.server.database.repository.VoteRepository;

import java.util.Optional;

public class ExecuteService extends BaseService implements BroadcastService {
    private final VoteRepository voteRepo = new VoteRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public ExecuteService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        Optional<String> targetId = voteRepo.resolveTarget(roomId);
        if (targetId.isEmpty()) return;

        String id = targetId.get();
        playerRepo.findById(roomId, id).ifPresent(p -> {
            playerRepo.kill(roomId, id);
            broadcaster.broadcast(roomId, new ExecuteMessage(id, p.name, p.role.name()));
        });

        voteRepo.reset(roomId);

        if (playerRepo.villagersWin(roomId) || playerRepo.wolvesWin(roomId)) {
            stateManager.setPhase(GamePhase.GAME_OVER);
            gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER);
        } else {
            stateManager.setPhase(GamePhase.NIGHT);
            stateManager.incrementNight();
            gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
        }
    }
}
