package src.server.service;

import src.server.GameStateManager;

// 村人チャット（全員閲覧・発言可）にメッセージを送信するサービス
public class SendVillageChatService extends BaseService {
    public SendVillageChatService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
