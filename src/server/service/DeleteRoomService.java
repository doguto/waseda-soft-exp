package src.server.service;

import src.message.DeleteRoomMessage;
import src.message.DeleteRoomResultMessage;
import src.server.database.repository.RoomRepository;
import src.server.game.GameMaster;

public class DeleteRoomService extends BaseService {

    public DeleteRoomService(String roomId, GameMaster gameMaster, RoomRepository roomRepository) {
        super(roomId, gameMaster, roomRepository);
    }

    public DeleteRoomResultMessage call(DeleteRoomMessage msg) {
        // RoomRepository.delete(roomId) でルームを削除する
        // 成功/失敗を DeleteRoomResultMessage に設定して返す
        boolean success = roomRepository.delete(roomId);
        return new DeleteRoomResultMessage(success);
    }
}
