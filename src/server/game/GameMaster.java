package src.server.game;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.core.Worker;
import src.server.database.entity.Player;
import src.server.database.entity.Role;
import src.server.database.repository.NightActionRepository;
import src.server.database.repository.PlayerRepository;
import src.server.database.repository.VoteRepository;

public class GameMaster {
    private final String roomId;
    private final BlockingQueue<ServiceType> queue = new LinkedBlockingQueue<>();
    private final GameStateManager stateManager;

    public final NightActionRepository nightActionRepository = new NightActionRepository();
    public final PlayerRepository playerRepository = new PlayerRepository();
    public final VoteRepository voteRepository = new VoteRepository();

    public GameMaster(String roomId) {
        this.roomId = roomId;
        this.stateManager = new GameStateManager(this);
    }

    public void pushService(ServiceType type) {
        queue.offer(type);
    }

    public void startWorker(Broadcaster broadcaster) {
        Thread workerThread = new Thread(new Worker(queue, this, broadcaster));
        workerThread.setDaemon(true);
        workerThread.start();
    }

    // ── condition checks (called by GameStateManager.check) ─────────────────

    public boolean allNightActionsComplete() {
        if (stateManager.isFirstNight()) {
            return nightActionRepository.getSeerTarget(roomId).isPresent();
        }
        List<Player> alive = playerRepository.getAlivePlayers(roomId);
        boolean hasWolf   = alive.stream().anyMatch(p -> p.role == Role.WOLF);
        boolean hasSeer   = alive.stream().anyMatch(p -> p.role == Role.SEER);
        boolean hasKnight = alive.stream().anyMatch(p -> p.role == Role.KNIGHT);

        if (hasWolf   && !nightActionRepository.allWolvesAttacked(roomId))      return false;
        if (hasSeer   && nightActionRepository.getSeerTarget(roomId).isEmpty())  return false;
        if (hasKnight && nightActionRepository.getKnightTarget(roomId).isEmpty())return false;
        return true;
    }

    public boolean allVoted() {
        return voteRepository.allVoted(roomId);
    }

    // ── getters ──────────────────────────────────────────────────────────────

    public String getRoomId()               { return roomId; }
    public GameStateManager getStateManager(){ return stateManager; }
}
