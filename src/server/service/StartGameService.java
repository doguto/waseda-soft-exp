package src.server.service;

import src.server.GameStateManager;

// ゲーム開始ボタン押下時にゲームを開始するサービス
public class StartGameService extends BaseService {
    public StartGameService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
