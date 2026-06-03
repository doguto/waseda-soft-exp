package src.server.service;

import src.message.StartGameMessage;
import src.message.StartGameResultMessage;
import src.common.GamePhase;
import src.server.core.Broadcaster;
import src.server.core.ServiceType;
import src.server.game.GameMaster;
import java.util.Set;

public class StartGameService extends BaseService {
    private final Broadcaster broadcaster;
    private final Set<String> activePlayerNames;

    public StartGameService(String roomId, GameMaster gameMaster, Broadcaster broadcaster, Set<String> activePlayerNames) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
        this.activePlayerNames = activePlayerNames;
    }

    public StartGameResultMessage call(StartGameMessage msg, String requester) {
        // 開始権限: ルーム作成者のみ
        String host = roomRepository.getHost(roomId);
        if (host == null || !host.equals(requester)) {
            return new StartGameResultMessage(false, "開始権限がありません", java.util.Collections.emptyList());
        }
    // ゲーム開始時点で実際に接続している人数だけを参加対象にする
    var room = src.server.database.GameDatabase.getInstance().getRoom(roomId);
    if (room == null) {
        return new StartGameResultMessage(false, "ルームが存在しません", java.util.Collections.emptyList());
    }

    room.players.removeIf(player -> !activePlayerNames.contains(player.name));

    if (room.players.size() < src.server.database.repository.RoomRepository.MIN_PLAYERS) {
            return new StartGameResultMessage(false, "プレイヤーが不足しています", java.util.Collections.emptyList());
        }
    if (room.players.size() > src.server.database.repository.RoomRepository.MAX_PLAYERS) {
            return new StartGameResultMessage(false,
                "プレイヤーが多すぎます（最大" + src.server.database.repository.RoomRepository.MAX_PLAYERS + "人）",
                java.util.Collections.emptyList());
        }
        // gameMaster.startWorker(broadcaster) でサービスキューのワーカースレッドを起動する
        gameMaster.startWorker(broadcaster);

        // ゲーム開始直後は WAITING のままだと再入室時にロビー扱いになるため、
        // 少なくとも「進行中」である状態へ進めておく。
        gameMaster.getStateManager().setPhase(GamePhase.DAY_DISCUSSION);

        // gameMaster.pushService(ServiceType.DISTRIBUTE_ROLE) をキューに積む
        gameMaster.pushService(ServiceType.DISTRIBUTE_ROLE);

        // 成功/失敗を StartGameResultMessage に設定して返す
        return new StartGameResultMessage(true, "ゲームを開始しました", gameMaster.playerRepository.getPlayerNames());
    }
}
