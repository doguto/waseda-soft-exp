package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.state.GamePhase;
import src.client.state.GameState;
import src.message.*;

public class NoonActionPresenter {
    private final GameState state;
    private final GameSession session;

    public NoonActionPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    public void requestEndDiscussion() {
        EndDiscussionMessage m = new EndDiscussionMessage();
        m.roomId = state.roomId;
        send(m);
    }

    public void sendVote(String target) {
        VoteMessage m = new VoteMessage();
        m.roomId = state.roomId;
        m.playerName = state.myName;
        m.targetName = target;
        send(m);
    }

    // --- サーバーメッセージハンドラ ---

    public void onEndDiscussionResult(JsonNode node) {
        if (node.get("success").asBoolean()) {
            state.phase = GamePhase.DAY_VOTE;
            log("[システム] 議論終了 → 投票フェーズへ");
            state.notifyListeners();
        }
    }

    public void onVoteResult(JsonNode node) {
        log("[投票] 投票を受け付けました");
        state.notifyListeners();
    }

    public void onDistributeVoteResult(JsonNode node) {
        String target = node.get("targetName").asText();
        log("[投票集計] 最多票: " + target);
        state.notifyListeners();
    }

    private void send(Object msg) {
        session.send(msg);
    }

    private void log(String msg) {
        state.chatLog.add(msg);
    }
}
