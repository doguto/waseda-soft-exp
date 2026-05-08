package src.server.service;

import src.message.DeleteRoomMessage;
import src.message.DeleteRoomResultMessage;
import src.server.GameMaster;

public class DeleteRoomService extends BaseService {

    public DeleteRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public DeleteRoomResultMessage call(DeleteRoomMessage msg) {
        // ルームを削除する
        return new DeleteRoomResultMessage();
    }
}
