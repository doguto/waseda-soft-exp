package src.server.service;

import src.message.StartGameMessage;
import src.message.StartGameResultMessage;
import src.server.core.Broadcaster;
import src.server.game.GameMaster;

public class StartGameService extends BaseService {
    private final Broadcaster broadcaster;

    public StartGameService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public StartGameResultMessage call(StartGameMessage msg) {
        // RoomRepository.canStart(roomId) で 4 人以上いるか確認する (不足時は失敗を返す)
        // gameMaster.startWorker(broadcaster) でサービスキューのワーカースレッドを起動する
        // gameMaster.pushService(ServiceType.DISTRIBUTE_ROLE) をキューに積む
        // 成功/失敗を StartGameResultMessage に設定して返す
        return new StartGameResultMessage();
    }
}
