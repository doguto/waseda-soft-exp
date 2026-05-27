package src.server.service;

import java.util.Optional;
import src.message.AnnounceMorningMessage;
import src.message.MediumResultMessage;
import src.message.SeerResultMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.common.Role;
import src.server.game.GameMaster;
import src.common.GamePhase;

public class AnnounceMorningService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public AnnounceMorningService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 1. 人狼の襲撃対象を取得する
        Optional<String> attackedOpt = gameMaster.nightActionRepository.resolveAttack();

        // 2. 騎士の護衛対象を取得する
        Optional<String> guardedOpt = gameMaster.nightActionRepository.getKnightTarget();

        String deadPlayerName = null;

        // 3. 襲撃対象と護衛対象を比較し、死亡者を決定する
        if (attackedOpt.isPresent()) {
            String attackedName = attackedOpt.get();

            boolean guarded = guardedOpt.isPresent()
                    && guardedOpt.get().equals(attackedName);

            if (!guarded) {
                gameMaster.playerRepository.kill(attackedName);
                deadPlayerName = attackedName;
            }
        }

        // 4. 朝の結果を全体通知する
        // MorningAnnouncementMessage が既にある前提。
        // もしコンストラクタが違う場合は、既存の定義に合わせて修正する。
        AnnounceMorningMessage announceMsg = new AnnounceMorningMessage();
        announceMsg.deadPlayerName = deadPlayerName;

        broadcaster.broadcast(roomId, announceMsg);
        // 朝の統一表現として「死体報告」を全体チャットにも流す
        String reportText;
        if (deadPlayerName != null) {
            reportText = "死体報告: " + deadPlayerName + " が発見されました";
        } else {
            reportText = "死体報告: 誰も死なず";
        }
        broadcaster.broadcast(roomId, new src.message.ChatBroadcastMessage("VILLAGE", "[朝]", reportText));

        // 5. 占い師に占い結果を通知する
        Optional<String> seerTargetOpt = gameMaster.nightActionRepository.getSeerTarget();
        if (seerTargetOpt.isPresent()) {
            String seerTargetName = seerTargetOpt.get();
            boolean isWolf = gameMaster.playerRepository.findByName(seerTargetName)
                    .map(p -> p.role == Role.WOLF).orElse(false);
            gameMaster.playerRepository.getAlivePlayers().stream()
                    .filter(p -> p.role == Role.SEER)
                    .findFirst()
                    .ifPresent(seer -> broadcaster.sendTo(seer.name, new SeerResultMessage(seerTargetName, isWolf)));
        }

        // 6. 霊媒師に前日処刑者の役職を通知する
        RoomData room = GameDatabase.getInstance().getRoom(roomId);
        if (room != null && room.executedPlayerName != null) {
            String executedName = room.executedPlayerName;
            boolean executedWasWolf = gameMaster.playerRepository.findByName(executedName)
                    .map(p -> p.role == Role.WOLF).orElse(false);
            gameMaster.playerRepository.getAlivePlayers().stream()
                    .filter(p -> p.role == Role.MEDIUM)
                    .findFirst()
                    .ifPresent(medium -> broadcaster.sendTo(medium.name, new MediumResultMessage(executedName, executedWasWolf)));
        }

        // 7. 勝利判定
        if (gameMaster.playerRepository.wolvesWin() || gameMaster.playerRepository.villagersWin()) {
            gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER);
        } else {
            // 朝の発表が終わったら明示的な昼開始サービスをキューに積む
            gameMaster.pushService(ServiceType.DAY_PHASE_START);
        }

        // 朝になったので前回の議論終了リクエストはリセットしてクライアントへ通知する
        try {
            int alive = gameMaster.playerRepository.getAlivePlayers().size();
            int need = (alive / 2) + 1;
            roomRepository.clearEndDiscussionRequests(roomId);
            broadcaster.broadcast(roomId, new src.message.EndDiscussionStatusMessage(0, alive, need));
        } catch (Exception e) {
            // ignore
        }

        // 8. 夜行動をリセットする（lastKnightTarget を保存してからリセット）
        gameMaster.nightActionRepository.updateLastKnightTarget();
        gameMaster.nightActionRepository.reset();
    }
}
