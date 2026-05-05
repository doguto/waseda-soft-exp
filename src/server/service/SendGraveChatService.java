package src.server.service;

import src.server.GameStateManager;

// 墓場チャット（死亡プレイヤーのみ発言可）にメッセージを送信するサービス
public class SendGraveChatService extends BaseService {
    public SendGraveChatService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
