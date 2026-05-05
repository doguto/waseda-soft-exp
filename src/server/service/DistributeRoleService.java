package src.server.service;

import src.server.GameStateManager;

// 全プレイヤーに役職を割り当てて通知するサービス（サーバー起点 broadcast）
public class DistributeRoleService extends BaseService {
    public DistributeRoleService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
