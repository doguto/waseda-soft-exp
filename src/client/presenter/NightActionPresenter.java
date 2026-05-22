package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.client.state.GameState;
import src.message.*;

import java.util.concurrent.CompletableFuture;

public class NightActionPresenter {
    private final GameState state;
    private final GameSession session;

    public NightActionPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    /** 襲撃リクエスト。受け付け確認を待って応答 node を返す。 */
    public CompletableFuture<JsonNode> sendWolfAttack(String target) {
        WolfAttackMessage m = new WolfAttackMessage();
        m.roomId = state.roomId;
        m.wolfName = state.myName;
        m.targetName = target;
        return session.sendRequest(m, WolfAttackResultMessage.MessageType)
            .thenApply(node -> {
                log("[夜] 襲撃を実行しました");
                state.notifyListeners();
                return node;
            });
    }

    /** 占いリクエスト。占い結果 (SeerResultMessage) の node を返す。 */
    public CompletableFuture<JsonNode> sendSeerInvestigate(String target) {
        SeerInvestigateMessage m = new SeerInvestigateMessage();
        m.roomId = state.roomId;
        m.seerName = state.myName;
        m.targetName = target;
        return session.sendRequest(m, SeerResultMessage.MessageType)
            .thenApply(node -> {
                String t = node.get("targetName").asText();
                boolean isWolf = node.get("isWolf").asBoolean();
                log("[占い結果] " + t + " は " + (isWolf ? "🐺 人狼！" : "村人側"));
                state.notifyListeners();
                return node;
            });
    }

    /** 守護リクエスト。受け付け確認を待って応答 node を返す。 */
    public CompletableFuture<JsonNode> sendKnightGuard(String target) {
        KnightGuardMessage m = new KnightGuardMessage();
        m.roomId = state.roomId;
        m.knightName = state.myName;
        m.targetName = target;
        return session.sendRequest(m, KnightGuardResultMessage.MessageType)
            .thenApply(node -> {
                log("[夜] 守護しました");
                state.notifyListeners();
                return node;
            });
    }

    // --- サーバーからの自発的なブロードキャストハンドラ ---

    public void onMediumResult(JsonNode node) {
        String target = node.get("targetName").asText();
        boolean isWolf = node.get("isWolf").asBoolean();
        log("[霊媒結果] " + target + " は " + (isWolf ? "🐺 人狼だった！" : "村人側だった"));
        state.notifyListeners();
    }

    private void log(String msg) {
        state.chatLog.add(msg);
    }
}
