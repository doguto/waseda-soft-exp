package src.server.service;

import src.message.CreateRoomMessage;
import src.message.CreateRoomResultMessage;
import src.server.game.GameMaster;

public class CreateRoomService extends BaseService {

    public CreateRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public CreateRoomResultMessage call(CreateRoomMessage msg) {
        // ルームを作成しホストプレイヤーを登録する
        return new CreateRoomResultMessage();
    }
}
