package src.server.service;

import src.message.JoinRoomMessage;
import src.message.JoinRoomResultMessage;
import src.server.GameMaster;

public class JoinRoomService extends BaseService {

    public JoinRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public JoinRoomResultMessage call(JoinRoomMessage msg) {
        // ルームの存在確認・重複名チェック後、プレイヤーを追加する
        return new JoinRoomResultMessage();
    }
}
