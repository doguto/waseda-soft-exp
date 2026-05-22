package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.state.GameState;
import src.message.*;

public class NightActionPresenter {
    private final GameState state;
    private final GameSession session;

    public NightActionPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    public void sendWolfAttack(String target) {
        WolfAttackMessage m = new WolfAttackMessage();
        m.roomId = state.roomId;
        m.wolfName = state.myName;
        m.targetName = target;
        send(m);
    }

    public void sendSeerInvestigate(String target) {
        SeerInvestigateMessage m = new SeerInvestigateMessage();
        m.roomId = state.roomId;
        m.seerName = state.myName;
        m.targetName = target;
        send(m);
    }

    public void sendKnightGuard(String target) {
        KnightGuardMessage m = new KnightGuardMessage();
        m.roomId = state.roomId;
        m.knightName = state.myName;
        m.targetName = target;
        send(m);
    }

    // --- サーバーメッセージハンドラ ---

    public void onSeerResult(JsonNode node) {
        String target = node.get("targetName").asText();
        boolean isWolf = node.get("isWolf").asBoolean();
        log("[占い結果] " + target + " は " + (isWolf ? "🐺 人狼！" : "村人側"));
        state.notifyListeners();
    }

    public void onMediumResult(JsonNode node) {
        String target = node.get("targetName").asText();
        boolean isWolf = node.get("isWolf").asBoolean();
        log("[霊媒結果] " + target + " は " + (isWolf ? "🐺 人狼だった！" : "村人側だった"));
        state.notifyListeners();
    }

    public void onWolfAttackResult(JsonNode node) {
        log("[夜] 襲撃を実行しました");
        state.notifyListeners();
    }

    public void onKnightGuardResult(JsonNode node) {
        log("[夜] 守護しました");
        state.notifyListeners();
    }

    public void onSeerInvestigateResult(JsonNode node) {
        // 結果は seer_result で返る
    }

    private void send(Object msg) {
        session.send(msg);
    }

    private void log(String msg) {
        state.chatLog.add(msg);
    }
}
