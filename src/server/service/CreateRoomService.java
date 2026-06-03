package src.server.service;

import src.message.CreateRoomMessage;
import src.message.CreateRoomResultMessage;
import src.server.database.entity.Player;
import src.server.game.GameMaster;

public class CreateRoomService extends BaseService {
    public CreateRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public CreateRoomResultMessage call(CreateRoomMessage msg) {
        boolean success = roomRepository.create(roomId);
        String message;
        if (success) {
            success = roomRepository.addPlayer(roomId, new Player(msg.name));
            roomRepository.setHost(roomId, msg.name);
            message = "SUCCESS : The room has been successfully created.\n"
                    + "Room ID : " + roomId + "\n"
                    + "Player Name :" + msg.name;
        } else {
            message = "そのルームIDはすでに使われています。別のルームIDを入力してください。";
        }
        return new CreateRoomResultMessage(success, message);
    }
}
