package src.server.service;

import src.server.GameStateManager;

// 議論終了ボタン押下またはタイマー切れで昼の議論を終了し、投票フェーズへ遷移するサービス
public class EndDiscussionService extends BaseService {
    public EndDiscussionService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
