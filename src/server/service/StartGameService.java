package src.server.service;

import src.message.StartGameMessage;
import src.message.StartGameResultMessage;
import src.server.Broadcaster;
import src.server.GameMaster;
import src.server.GamePhase;
import src.server.ServiceType;
import src.server.database.repository.RoomRepository;

public class StartGameService extends BaseService {
    private final RoomRepository roomRepo = new RoomRepository();
    private final Broadcaster broadcaster;

    public StartGameService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public StartGameResultMessage call(StartGameMessage msg) {
        if (!roomRepo.canStart(msg.roomId)) {
            return new StartGameResultMessage(false, "ゲーム開始には4人以上必要です");
        }
        stateManager.setPhase(GamePhase.NIGHT);
        stateManager.incrementNight();
        gameMaster.startWorker(broadcaster);
        gameMaster.pushService(ServiceType.DISTRIBUTE_ROLE);
        return new StartGameResultMessage(true, "ゲームを開始します");
    }
}
