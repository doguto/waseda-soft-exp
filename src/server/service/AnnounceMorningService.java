package src.server.service;

import src.server.Broadcaster;
import src.server.BroadcastService;
import src.server.GameMaster;

public class AnnounceMorningService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public AnnounceMorningService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 狼の攻撃と騎士の護衛を照合して死亡者を決定し朝を告知、占い結果を占い師に送信する
    }
}
