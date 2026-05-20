package src.server.game;

import java.util.concurrent.atomic.AtomicBoolean;
import src.server.core.ServiceType;

// per-room state container. check() reads repos and delegates pushService() to GameMaster.
public class GameStateManager {
    private GamePhase currentPhase = GamePhase.WAITING;
    private int nightCount = 0;
    private final AtomicBoolean voteResolved    = new AtomicBoolean(false);
    private final AtomicBoolean discussionEnded = new AtomicBoolean(false);

    private final GameMaster gameMaster;

    public GameStateManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    public void check(GameEvent event) {
        switch (event) {
            case NIGHT_ACTION_SUBMITTED -> {
                if (gameMaster.allNightActionsComplete()) {
                    gameMaster.pushService(ServiceType.ANNOUNCE_MORNING);
                }
            }
            case VOTE_SUBMITTED -> {
                if (gameMaster.allVoted()) {
                    if (voteResolved.compareAndSet(false, true)) {
                        gameMaster.pushService(ServiceType.DISTRIBUTE_VOTE_RESULT);
                    }
                }
            }
            case DISCUSSION_ENDED -> {
                if (discussionEnded.compareAndSet(false, true)) {
                    currentPhase = GamePhase.VOTE;
                    gameMaster.pushService(ServiceType.VOTE_PHASE_START);
                }
            }
        }
    }

    // ── state getters / setters ──────────────────────────────────────────────

    public GamePhase getCurrentPhase()            { return currentPhase; }
    public void setPhase(GamePhase phase)          { this.currentPhase = phase; }
    public int  getNightCount()                    { return nightCount; }
    public void incrementNight()                   { nightCount++; }
    public boolean isFirstNight()                  { return nightCount == 1; }

    public void resetRoundState() {
        voteResolved.set(false);
        discussionEnded.set(false);
    }
}
