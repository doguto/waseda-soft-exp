package src.server.service;

import java.util.Optional;
import src.message.ExecuteMessage;
import src.server.core.BroadcastService;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.Player;
import src.server.database.repository.VoteRepository.VoteResolution;
import src.server.game.GameMaster;
import src.common.GamePhase;

public class ExecuteService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public ExecuteService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // DistributeVoteResultService で既に決めた処刑対象があればそれを使う。
        // なければ念のため resolveTarget() で決定する（互換性のためのフォールバック）。
        Optional<String> targetName = gameMaster.voteRepository.getResolvedTarget();
        VoteResolution resolution = null;
        if (targetName.isEmpty()) {
            resolution = gameMaster.voteRepository.resolveTarget();
            targetName = resolution.target();
        }

        RoomData room = GameDatabase.getInstance().getRoom(roomId);
        if (room != null) {
            room.executedPlayerName = targetName.orElse(null);
            room.executeReadyPlayers.clear();
        }

        String executedRole = null;
        if (targetName.isPresent()) {
            Optional<Player> targetPlayer = gameMaster.playerRepository.findByName(targetName.get());
            executedRole = targetPlayer.map(player -> player.role.name()).orElse(null);
            gameMaster.playerRepository.kill(targetName.get());
        }

        // VoteRepository.reset(roomId) で投票と保持された解決対象をリセットする
        gameMaster.voteRepository.reset();

        // PlayerRepository.wolvesWin / villagersWin で勝利判定を行う
        if (gameMaster.playerRepository.villagersWin() || gameMaster.playerRepository.wolvesWin()) {
            // 勝利なら処刑を通知してすぐゲーム終了へ遷移する
            broadcaster.broadcast(roomId, new ExecuteMessage(targetName.orElse(null), executedRole));
            gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER);
        } else {
            // 続行なら EXECUTEフェーズへ移行し、全クライアントの演出完了を待つ
            stateManager.setPhase(GamePhase.EXECUTE);
            broadcaster.broadcast(roomId, new ExecuteMessage(targetName.orElse(null), executedRole));
            // 各クライアントが ExecuteReadyMessage を送信したら ExecuteReadyService が NIGHT_PHASE_START を積む
        }
    }
}
