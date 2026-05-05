package src.server.service;

import src.server.GameStateManager;

// 投票集計後に最多票のプレイヤーをゲームから除外（処刑）し、勝利判定へ進むサービス（サーバー起点 broadcast）
public class ExecuteService extends BaseService {
    public ExecuteService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
