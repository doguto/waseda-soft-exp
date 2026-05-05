package src.server.service;

import src.server.GameStateManager;

// 全員投票完了または投票タイマー切れ後に投票結果を集計して全員に通知するサービス（サーバー起点 broadcast）
public class DistributeVoteResultService extends BaseService {
    public DistributeVoteResultService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
