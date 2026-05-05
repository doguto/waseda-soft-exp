package src.server.service;

import src.server.GameStateManager;

// 朝フェーズの開始時に前夜の出来事（死亡者など）を全員に通知するサービス（サーバー起点 broadcast）
public class AnnounceMorningService extends BaseService {
    public AnnounceMorningService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
