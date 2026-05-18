package src.server.service;

import src.message.AnnounceGameOverMessage;
import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.repository.PlayerRepository;
import src.server.game.GameMaster;

import java.util.List;

public class AnnounceGameOverService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;
    private final PlayerRepository playerRepo = new PlayerRepository();

    public AnnounceGameOverService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // PlayerRepository.wolvesWin(roomId) / villagersWin(roomId) で勝利陣営 (WOLF / VILLAGER) を判定する
        String winner = playerRepo.villagersWin(roomId) ? "VILLAGER" : "WOLF";

        // RoomData.players から全プレイヤーの名前とロールを取得する
        RoomData room = GameDatabase.getInstance().getRoom(roomId);
        List<AnnounceGameOverMessage.PlayerResult> results = room == null
            ? List.of()
            : room.players.stream()
                .map(player -> new AnnounceGameOverMessage.PlayerResult(player.name, player.role.name()))
                .toList();

        // 勝利陣営と全プレイヤーのロール一覧を含むメッセージをルーム全体へ通知する
        // 人狼陣営が勝利した場合、CRAZY_VILLAGER も人狼陣営の勝者として扱われるのはクライアント側の表示で解釈する
        broadcaster.broadcast(roomId, new AnnounceGameOverMessage(winner, results));
    }
}
