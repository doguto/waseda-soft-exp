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
            Optional<String> attackedName = nightActionRepo.resolveAttack(roomId);
            Optional<String> guardedName  = nightActionRepo.getKnightTarget(roomId);

            if (attackedName.isPresent()) {
                boolean guarded = guardedName.map(g -> g.equals(attackedName.get())).orElse(false);
                if (!guarded) {
                    playerRepo.kill(roomId, attackedName.get());
                    playerRepo.findByName(roomId, attackedName.get()).ifPresent(p -> {
                        msg.deadPlayerName = p.name;
                    });
                }
            }
        }

        broadcaster.broadcast(roomId, msg);
        nightActionRepo.reset(roomId);

        // 占い師のみに調査結果を送信（翌朝通知）
        nightActionRepo.getSeerTarget(roomId).ifPresent(targetName ->
            playerRepo.findByName(roomId, targetName).ifPresent(target -> {
                boolean isWolf = target.role == Role.WOLF;
                playerRepo.getAlivePlayers(roomId).stream()
                    .filter(p -> p.role == Role.SEER)
                    .findFirst()
                    .ifPresent(seer ->
                        broadcaster.sendTo(seer.name, new SeerResultMessage(target.name, isWolf))
                    );
            })
        );

        stateManager.setPhase(GamePhase.DISCUSSION);
        stateManager.resetRoundState();
    }
}
