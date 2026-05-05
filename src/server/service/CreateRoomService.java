package src.server.service;

import src.server.GameStateManager;

// ルームを新規作成するサービス
public class CreateRoomService extends BaseService {
    public CreateRoomService(String roomId, GameStateManager stateManager) {
        super(roomId, stateManager);
    }

    public void call() {
    }
}
