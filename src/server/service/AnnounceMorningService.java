package src.server.service;

import java.util.Optional;
import src.message.AnnounceMorningMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.game.GameMaster;
import src.server.game.GamePhase;

public class AnnounceMorningService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public AnnounceMorningService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 1. 人狼の襲撃対象を取得する
        Optional<String> attackedOpt = gameMaster.nightActionRepository.resolveAttack(roomId);

        // 2. 騎士の護衛対象を取得する
        Optional<String> guardedOpt = gameMaster.nightActionRepository.getKnightTarget(roomId);

        String deadPlayerName = null;

        // 3. 襲撃対象と護衛対象を比較し、死亡者を決定する
        if (attackedOpt.isPresent()) {
            String attackedName = attackedOpt.get();

            boolean guarded = guardedOpt.isPresent()
                    && guardedOpt.get().equals(attackedName);

            if (!guarded) {
                gameMaster.playerRepository.kill(roomId, attackedName);
                deadPlayerName = attackedName;
            }
        }

        // 4. 朝の結果を全体通知する
        // MorningAnnouncementMessage が既にある前提。
        // もしコンストラクタが違う場合は、既存の定義に合わせて修正する。
        AnnounceMorningMessage announceMsg = new AnnounceMorningMessage();
        announceMsg.deadPlayerName = deadPlayerName;

        broadcaster.broadcastAlive(roomId, announceMsg);

        // 5. 占い結果通知は後で追加
        // TODO:
        // gameMaster.nightActionRepository.getSeerTarget(roomId) で占い先を取得
        // gameMaster.playerRepository.findByName(roomId, targetName) で役職確認
        // 占い師本人にだけ gameMaster.broadcaster.sendTo(...) で通知

        // 6. 霊媒師通知は後で追加
        // TODO:
        // 前日に処刑されたプレイヤーの役職を霊媒師にだけ通知

        // 7. 勝利判定
        if (gameMaster.playerRepository.wolvesWin(roomId) || gameMaster.playerRepository.villagersWin(roomId)) {
            gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER);
        } else {
            gameMaster.getStateManager().setPhase(GamePhase.DISCUSSION);
        }

        // 8. 夜行動をリセットする
        gameMaster.nightActionRepository.reset(roomId);
    }
}
