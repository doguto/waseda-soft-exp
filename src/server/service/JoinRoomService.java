package src.server.service;

import src.message.JoinRoomMessage;
import src.message.JoinRoomResultMessage;
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
        return new JoinRoomResultMessage();
    }
}
