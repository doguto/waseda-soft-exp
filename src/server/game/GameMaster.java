package src.server.game;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.core.Worker;
import src.server.database.entity.Player;
import src.common.Role;
import src.server.database.repository.ChatRepository;
import src.server.database.repository.NightActionRepository;
import src.server.database.repository.PlayerRepository;
import src.server.database.repository.VoteRepository;

public class GameMaster {
    /** 朝フェーズ（結果発表）の表示時間(ms)。経過後に昼へ移行する。初日・通常朝で共通。 */
    public static final long MORNING_PHASE_MILLIS = 5000;

    private final String roomId;
    private final BlockingQueue<ServiceType> queue = new LinkedBlockingQueue<>();
    private final GameStateManager stateManager;
    // 遅延サービス投入用（朝フェーズの一定時間表示など）。ルームごとに1スレッド。
    private final ScheduledExecutorService scheduler;

    public final NightActionRepository nightActionRepository;
    public final PlayerRepository playerRepository;
    public final VoteRepository voteRepository;
    public final ChatRepository chatRepository;

    public GameMaster(String roomId) {
        this.roomId = roomId;
        this.nightActionRepository = new NightActionRepository(roomId);
        this.playerRepository = new PlayerRepository(roomId);
        this.voteRepository = new VoteRepository(roomId);
        this.chatRepository = new ChatRepository(roomId);
        this.stateManager = new GameStateManager(this);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "game-scheduler-" + roomId);
            t.setDaemon(true);
            return t;
        });
    }

    // ── helper transitions ─────────────────────────────────────────────────
    public void startInitialDay() {
        // 初日の配役後に昼へ移行するための共通処理
        stateManager.beginDayDiscussion();
    }

    public void pushService(ServiceType type) {
        queue.offer(type);
    }

    /** 指定ミリ秒後に Worker のキューへサービスを投入する（Worker スレッドはブロックしない）。 */
    public void scheduleService(ServiceType type, long delayMillis) {
        scheduler.schedule(() -> queue.offer(type), delayMillis, TimeUnit.MILLISECONDS);
    }

    public void startWorker(Broadcaster broadcaster) {
        Thread workerThread = new Thread(new Worker(queue, this, broadcaster));
        workerThread.setDaemon(true);
        workerThread.start();
    }

    // ── condition checks (called by GameStateManager.check) ─────────────────

    public boolean allNightActionsComplete() {
        // Do not short-circuit on the first night: require all present roles
        // to submit their actions before advancing. The previous special-case
        // caused the Seer alone to trigger the morning when wolves/knight
        // existed but hadn't acted yet.
        List<Player> alive = playerRepository.getAlivePlayers();
        boolean hasWolf   = alive.stream().anyMatch(p -> p.role == Role.WOLF);
        boolean hasSeer   = alive.stream().anyMatch(p -> p.role == Role.SEER);
        boolean hasKnight = alive.stream().anyMatch(p -> p.role == Role.KNIGHT);

        if (hasWolf   && !nightActionRepository.allWolvesAttacked())      return false;
        if (hasSeer   && nightActionRepository.getSeerTarget().isEmpty())  return false;
        if (hasKnight && nightActionRepository.getKnightTarget().isEmpty())return false;
        return true;
    }

    public boolean allVoted() {
        return voteRepository.allVoted();
    }

    public boolean allExecuteReady(src.server.database.RoomData room) {
        return room.executeReadyPlayers.size() >= playerRepository.getPlayerNames().size();
    }

    // ── getters ──────────────────────────────────────────────────────────────

    public String getRoomId()               { return roomId; }
    public GameStateManager getStateManager(){ return stateManager; }
}
