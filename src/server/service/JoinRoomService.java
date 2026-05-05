package src.server.service;

import src.server.GameStateManager;

// 既存のルームに参加するサービス
public class JoinRoomService extends BaseService {
    public JoinRoomService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
