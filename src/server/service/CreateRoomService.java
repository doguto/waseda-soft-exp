package src.server.service;

import src.message.CreateRoomMessage;
import src.message.CreateRoomResultMessage;
import src.server.database.entity.Player;
import src.server.database.repository.RoomRepository;
import src.server.game.GameMaster;

public class CreateRoomService extends BaseService {
    public CreateRoomService(String roomId, GameMaster gameMaster, RoomRepository roomRepository) {
        super(roomId, gameMaster, roomRepository);
    }

    public CreateRoomResultMessage call(CreateRoomMessage msg) {
        // RoomRepository.create(roomId) でルームを作成する
        // RoomRepository.addPlayer(roomId, new Player(msg.playerName)) でホストを登録する
        // 成功/失敗を CreateRoomResultMessage に設定して返す
        boolean success = roomRepository.create(roomId);
        String message;
        if (success) {
            success = roomRepository.addPlayer(roomId, new Player(msg.name));
            message = "SUCCESS : The room has been successfully created.\n"
                    + "Room ID : " + roomId + "\n"
                    + "Player Name :" + msg.name;
        } else {
            message = "ERROR : The Room ID has already been used.";
        }
        return new CreateRoomResultMessage(success, message);
    }
}
