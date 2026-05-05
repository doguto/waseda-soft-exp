package src.server.service;

import src.server.GameStateManager;

// 昼フェーズにプレイヤーが投票対象を選択して票を登録するサービス
public class VoteService extends BaseService {
    public VoteService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
