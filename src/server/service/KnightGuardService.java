package src.server.service;

import src.server.GameStateManager;

// 夜フェーズに騎士が護衛対象を選択するサービス（前夜と同対象は無効）
public class KnightGuardService extends BaseService {
    public KnightGuardService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
