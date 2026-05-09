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
        // 最低人数確認後、ゲームを開始してロール配布サービスをキューに積む
        return new StartGameResultMessage();
    }
}
