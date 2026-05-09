package src.server.service;

import src.message.CreateRoomMessage;
import src.message.CreateRoomResultMessage;
import src.server.game.GameMaster;

public class CreateRoomService extends BaseService {

    public CreateRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public CreateRoomResultMessage call(CreateRoomMessage msg) {
        // RoomRepository.create(roomId) でルームを作成する
        // RoomRepository.addPlayer(roomId, new Player(msg.playerName)) でホストを登録する
        // 成功/失敗を CreateRoomResultMessage に設定して返す
        return new CreateRoomResultMessage();
    }
}
