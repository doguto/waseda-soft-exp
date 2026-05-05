package src.server.service;

import src.server.GameStateManager;

// ルームを削除するサービス
public class DeleteRoomService extends BaseService {
    public DeleteRoomService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
