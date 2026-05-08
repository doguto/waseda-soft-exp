package src.server.service;

import src.message.AnnounceMorningMessage;
import src.message.SeerResultMessage;
import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;
import src.server.GamePhase;
import src.server.database.entity.Player;
import src.server.database.entity.Role;
import src.server.database.repository.NightActionRepository;
import src.server.database.repository.PlayerRepository;

import java.util.Optional;

public class AnnounceMorningService extends BaseService implements BroadcastService {
    private final NightActionRepository nightActionRepo = new NightActionRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public AnnounceMorningService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        stateManager.setPhase(GamePhase.MORNING);

        AnnounceMorningMessage msg = new AnnounceMorningMessage();

        if (!stateManager.isFirstNight()) {
            Optional<String> attackedId = nightActionRepo.resolveAttack(roomId);
            Optional<String> guardedId  = nightActionRepo.getKnightTarget(roomId);

            if (attackedId.isPresent()) {
                boolean guarded = guardedId.map(g -> g.equals(attackedId.get())).orElse(false);
                if (!guarded) {
                    playerRepo.kill(roomId, attackedId.get());
                    playerRepo.findById(roomId, attackedId.get()).ifPresent(p -> {
                        msg.deadPlayerId   = p.id;
                        msg.deadPlayerName = p.name;
                    });
                }
            }
        }

        broadcaster.broadcast(roomId, msg);
        nightActionRepo.reset(roomId);

        // 占い師のみに調査結果を送信（翌朝通知）
        nightActionRepo.getSeerTarget(roomId).ifPresent(targetId ->
            playerRepo.findById(roomId, targetId).ifPresent(target -> {
                boolean isWolf = target.role == Role.WOLF;
                playerRepo.getAlivePlayers(roomId).stream()
                    .filter(p -> p.role == Role.SEER)
                    .findFirst()
                    .ifPresent(seer ->
                        broadcaster.sendTo(seer.id, new SeerResultMessage(target.id, target.name, isWolf))
                    );
            })
        );

        stateManager.setPhase(GamePhase.DISCUSSION);
        stateManager.resetRoundState();
    }
}
