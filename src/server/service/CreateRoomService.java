package src.server.service;

import src.message.CreateRoomMessage;
import src.message.CreateRoomResultMessage;
import src.server.GameMaster;
import src.server.GamePhase;
import src.server.database.entity.Player;
import src.server.database.repository.RoomRepository;

public class CreateRoomService extends BaseService {
    private final RoomRepository roomRepo = new RoomRepository();

    public CreateRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public CreateRoomResultMessage call(CreateRoomMessage msg) {
        boolean created = roomRepo.create(msg.roomId);
        if (!created) {
            return new CreateRoomResultMessage(false, "ルームID が既に存在するわ");
        }
        roomRepo.addPlayer(msg.roomId, new Player(msg.playerId, msg.name));
        stateManager.setPhase(GamePhase.WAITING);
        return new CreateRoomResultMessage(true, "ルームを作成したわ");
    }
}
