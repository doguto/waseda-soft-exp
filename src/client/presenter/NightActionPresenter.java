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
        if (state.hasNightActionSent) {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        WolfAttackMessage m = new WolfAttackMessage();
        m.roomId = state.roomId;
        m.wolfName = state.myName;
        m.targetName = target;
        return session.sendRequest(m, WolfAttackResultMessage.MessageType)
            .thenApply(node -> {
                boolean success = node.has("success") ? node.get("success").asBoolean() : true;
                if (success) {
                    state.hasNightActionSent = true;
                    log("[夜] 襲撃を実行しました");
                } else {
                    String message = node.has("message") ? node.get("message").asText() : "襲撃済みです";
                    log("[夜] " + message);
                }
                state.notifyListeners();
                return node;
            });
    }

    /** 占いリクエスト。占い結果 (SeerResultMessage) の node を返す。 */
    public CompletableFuture<JsonNode> sendSeerInvestigate(String target) {
        if (state.hasNightActionSent) {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        SeerInvestigateMessage m = new SeerInvestigateMessage();
        m.roomId = state.roomId;
        m.seerName = state.myName;
        m.targetName = target;
        // 即時フィードバック: 押下したことが分かるようチャットログに記録して UI を更新
        log("[夜] 占い送信: " + target);
        state.notifyListeners();

        return session.sendRequest(m, SeerResultMessage.MessageType)
            .thenApply(node -> {
                boolean success = node.has("success") ? node.get("success").asBoolean() : true;
                if (success) {
                    state.hasNightActionSent = true;
                    String t = node.get("targetName").asText();
                    boolean isWolf = node.get("isWolf").asBoolean();
                    log("[占い結果] " + t + " は " + (isWolf ? "🐺 人狼！" : "村人側"));
                } else {
                    String message = node.has("message") ? node.get("message").asText() : "占い済みです";
                    log("[夜] " + message);
                }
                state.notifyListeners();
                return node;
            });
    }

    /** 守護リクエスト。受け付け確認を待って応答 node を返す。 */
    public CompletableFuture<JsonNode> sendKnightGuard(String target) {
        if (state.hasNightActionSent) {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        KnightGuardMessage m = new KnightGuardMessage();
        m.roomId = state.roomId;
        m.knightName = state.myName;
        m.targetName = target;
        return session.sendRequest(m, KnightGuardResultMessage.MessageType)
            .thenApply(node -> {
                boolean success = node.has("success") ? node.get("success").asBoolean() : true;
                if (success) {
                    state.hasNightActionSent = true;
                    log("[夜] 守護しました");
                } else {
                    String message = node.has("message") ? node.get("message").asText() : "守護済みです";
                    log("[夜] " + message);
                }
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
