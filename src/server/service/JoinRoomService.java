package src.server.service;

import java.util.List;
import src.message.JoinRoomMessage;
import src.message.JoinRoomResultMessage;
import src.server.database.entity.Player;
import src.server.game.GameMaster;

public class JoinRoomService extends BaseService {

    public JoinRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public JoinRoomResultMessage call(JoinRoomMessage msg) {
        // RoomRepository.exists(roomId) でルームの存在を確認する
        // RoomRepository.getPlayers(roomId) で同名プレイヤーがいないかチェックする
        // RoomRepository.addPlayer(roomId, new Player(msg.playerName)) でプレイヤーを追加する
        // 成功/失敗を JoinRoomResultMessage に設定して返す
        boolean success = roomRepository.exists(roomId);
        String message;

        if (success) {
            List<Player> players = roomRepository.getPlayers(roomId);
            for (Player player : players) {
                if (player.name.equals(msg.name)) {
                    success = false;
                    break;
                }
            }
            if (success) {
                roomRepository.addPlayer(roomId, new Player(msg.name));
                message = "SUCCESS : You have successfully entered the room.\n"
                        + "Room ID : " + roomId + "\n"
                        + "Player Name : " + msg.name;
            } else {
                message = "ERROR : A player with the same name already exists in the room.";
            }
        } else {
            message = "ERROR : No room exists with the specified Room ID.";
        }

        return new JoinRoomResultMessage(success, message);
    }
}
