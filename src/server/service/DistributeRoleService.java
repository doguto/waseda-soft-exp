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
        // 人数に応じたロールリストをシャッフルして各プレイヤーに通知する
    }
}
