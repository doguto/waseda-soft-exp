package src.server.service;

import src.server.GameStateManager;

// 夜フェーズに占い師が調査対象を選択し、翌朝に人狼か人間かを通知するサービス
public class SeerInvestigateService extends BaseService {
    public SeerInvestigateService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
