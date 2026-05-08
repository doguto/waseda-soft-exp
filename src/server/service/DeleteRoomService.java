package src.server.service;

import src.message.DeleteRoomMessage;
import src.message.DeleteRoomResultMessage;
import src.server.GameMaster;
import src.server.database.repository.RoomRepository;

public class DeleteRoomService extends BaseService {
    private final RoomRepository roomRepo = new RoomRepository();

    public DeleteRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public DeleteRoomResultMessage call(DeleteRoomMessage msg) {
        boolean deleted = roomRepo.delete(msg.roomId);
        return new DeleteRoomResultMessage(deleted);
    }
}
