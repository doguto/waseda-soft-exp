package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;

public class DistributeRoleService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public DistributeRoleService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 人数に応じたロールリストをシャッフルして各プレイヤーに通知する
    }
}
