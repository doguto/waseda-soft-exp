package src.server.service;

import src.server.GameStateManager;

// 人狼チャット（人狼のみ発言可）にメッセージを送信するサービス
public class SendWolfChatService extends BaseService {
    public SendWolfChatService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
