package src.server.service;

import src.server.GameStateManager;

// 夜フェーズに人狼が襲撃対象を選択するサービス
public class WolfAttackService extends BaseService {
    public WolfAttackService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
