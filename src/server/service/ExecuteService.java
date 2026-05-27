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
        }

        String executedRole = null;
        if (targetName.isPresent()) {
            Optional<Player> targetPlayer = gameMaster.playerRepository.findByName(targetName.get());
            executedRole = targetPlayer.map(player -> player.role.name()).orElse(null);
            gameMaster.playerRepository.kill(targetName.get());
        }

        // 処刑者の名前とロールはルーム内の全員に通知する
        broadcaster.broadcast(roomId, new ExecuteMessage(targetName.orElse(null), executedRole));

        // VoteRepository.reset(roomId) で投票と保持された解決対象をリセットする
        gameMaster.voteRepository.reset();

        // PlayerRepository.wolvesWin / villagersWin で勝利判定を行う
        if (gameMaster.playerRepository.villagersWin() || gameMaster.playerRepository.wolvesWin()) {
            // 勝利なら gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER) をキューに積む
            gameMaster.pushService(ServiceType.ANNOUNCE_GAME_OVER);
        } else {
            // 続行なら gameMaster.pushService(ServiceType.NIGHT_PHASE_START) をキューに積む
            gameMaster.pushService(ServiceType.NIGHT_PHASE_START);
        }
    }
}
