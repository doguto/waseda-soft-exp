package src.server.service;

import src.message.JoinRoomMessage;
import src.message.JoinRoomResultMessage;
import src.server.GameMaster;
import src.server.database.entity.Player;
import src.server.database.repository.RoomRepository;

public class JoinRoomService extends BaseService {
    private final RoomRepository roomRepo = new RoomRepository();

    public JoinRoomService(String roomId, GameMaster gameMaster) {
        super(roomId, gameMaster);
    }

    public JoinRoomResultMessage call(JoinRoomMessage msg) {
        if (!roomRepo.exists(msg.roomId)) {
            return new JoinRoomResultMessage(false, "ルームが見つかりません");
        }
        boolean nameExists = roomRepo.getPlayers(msg.roomId).stream()
            .anyMatch(p -> p.name.equals(msg.name));
        if (nameExists) {
            return new JoinRoomResultMessage(false, "同じ名前のプレイヤーが既に存在します");
        }
        boolean added = roomRepo.addPlayer(msg.roomId, new Player(msg.name));
        if (!added) {
            return new JoinRoomResultMessage(false, "ルームへの参加に失敗しました");
        }
        return new JoinRoomResultMessage(true, "ルームに参加しました");
    }
}
