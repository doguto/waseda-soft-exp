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
        // RoomRepository.exists(roomId) でルームの存在を確認する
        // RoomRepository.getPlayers(roomId) で同名プレイヤーがいないかチェックする
        // RoomRepository.addPlayer(roomId, new Player(msg.playerName)) でプレイヤーを追加する
        // 成功/失敗を JoinRoomResultMessage に設定して返す
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
                // 既に名前が存在する場合は再入室扱いにできるかを優先して判定
                if (allowRejoin) {
                    message = "SUCCESS : Rejoined room.";
                    success = true;
                } else {
                    success = false;
                    message = "同一名称のプレイヤーがいます。別の名前を入力してください。";
                }
            } else {
                // 新規参加の場合はゲームが既に開始していると拒否する
                if (gameMaster != null && gameMaster.getStateManager().getCurrentPhase() != src.common.GamePhase.WAITING) {
                    return new JoinRoomResultMessage(false, "ゲームは既に開始されています");
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
            message = "ERROR : No room exists with the specified Room ID.";
        }

        return new JoinRoomResultMessage(success, message);
    }
}
