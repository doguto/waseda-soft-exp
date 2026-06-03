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

    public JoinRoomResultMessage call(JoinRoomMessage msg, boolean allowRejoin) {
        boolean success = roomRepository.exists(roomId);
        String message;

        if (success) {
            List<Player> players = roomRepository.getPlayers(roomId);
            boolean found = false;
            for (Player player : players) {
                if (player.name.equals(msg.name)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                if (allowRejoin) {
                    message = "SUCCESS : Rejoined room.";
                    success = true;
                } else {
                    success = false;
                    message = "同一名称のプレイヤーがいます。別の名前を入力してください。";
                }
            } else {
                if (gameMaster != null && gameMaster.getStateManager().getCurrentPhase() != src.common.GamePhase.WAITING) {
                    return new JoinRoomResultMessage(false, "ゲームは既に開始されています。");
                }
                if (roomRepository.isFull(roomId)) {
                    return new JoinRoomResultMessage(false,
                        "ルームが満員です(最大" + src.server.database.repository.RoomRepository.MAX_PLAYERS + "人)。");
                }
                success = roomRepository.addPlayer(roomId, new Player(msg.name));
                if (success) {
                    message = "SUCCESS : You have successfully entered the room.\n"
                            + "Room ID : " + roomId + "\n"
                            + "Player Name : " + msg.name;
                } else {
                    message = "ERROR : Failed to add player.";
                }
            }
        } else {
            message = "そのルームIDは存在しません。ルームIDを再確認してください。";
        }

        return new JoinRoomResultMessage(success, message);
    }
}
