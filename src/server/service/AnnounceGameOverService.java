package src.server.service;

import src.server.GameStateManager;

// 勝利条件成立時にゲーム終了と勝利陣営を全員に通知するサービス（サーバー起点 broadcast）
public class AnnounceGameOverService extends BaseService {
    public AnnounceGameOverService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
