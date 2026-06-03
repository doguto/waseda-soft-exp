package src.server.service;

import src.message.DayPhaseStartMessage;
import src.message.EndDiscussionStatusMessage;
import src.server.core.Broadcaster;
import src.server.core.BroadcastService;
import src.server.game.GameMaster;

public class DayPhaseStartService extends BaseService implements BroadcastService {
    private final Broadcaster broadcaster;

    public DayPhaseStartService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    @Override
    public void call() {
        // 中央マネージャで昼（議論）フェーズ開始処理を行う
        gameMaster.getStateManager().beginDayDiscussion();
        int alive = gameMaster.playerRepository.getAlivePlayers().size();
        int need = (alive / 2) + 1;
        broadcaster.broadcast(roomId, new DayPhaseStartMessage(0, alive, need));

        // 初期表示用に、過半数計算を持った状態をクライアントへ送る
        broadcaster.broadcast(roomId, new EndDiscussionStatusMessage(0, alive, need));
    }
}
