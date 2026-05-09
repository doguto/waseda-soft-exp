package src.server.service;

import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class DistributeRoleService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public DistributeRoleService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // RoomRepository.getPlayers(roomId) で全プレイヤーを取得する
        // 人数に応じたロール構成のリスト (例: 4人→WOLF×1, SEER×1, KNIGHT×1, VILLAGER×1) を作成する
        // Collections.shuffle() でリストをシャッフルする
        // PlayerRepository.setRole(roomId, playerName, role) で各プレイヤーにロールを割り当てる
        // broadcaster でロールを各プレイヤーにユニキャスト通知する
        // gameMaster.pushService(ServiceType.NIGHT_PHASE_START) をキューに積む
    }
}
