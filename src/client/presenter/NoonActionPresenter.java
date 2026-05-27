package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.network.GameSession;
import src.common.GamePhase;
import src.client.state.GameState;
import src.message.*;

import java.util.concurrent.CompletableFuture;
import java.util.StringJoiner;

public class NoonActionPresenter {
    private final GameState state;
    private final GameSession session;

    public NoonActionPresenter(GameState state, GameSession session) {
        this.state   = state;
        this.session = session;
    }

    /** 議論終了リクエスト。成功時に投票フェーズへ遷移して true を返す。 */
    public CompletableFuture<Boolean> requestEndDiscussion() {
        EndDiscussionMessage m = new EndDiscussionMessage();
        m.roomId = state.roomId;
        return session.sendRequest(m, EndDiscussionResultMessage.MessageType)
            .thenApply(node -> {
                boolean ok = node.get("success").asBoolean();
                if (ok) {
                    state.phase = GamePhase.DAY_VOTE;
                    log("[システム] 議論終了 → 投票フェーズへ");
                }
                state.notifyListeners();
                return ok;
            });
    }

    /** 投票リクエスト。受け付け確認を待って true を返す。 */
    public CompletableFuture<Boolean> sendVote(String target) {
        VoteMessage m = new VoteMessage();
        m.roomId = state.roomId;
        m.playerName = state.myName;
        m.targetName = target;
        return session.sendRequest(m, VoteResultMessage.MessageType)
            .thenApply(node -> {
                log("[投票] 投票を受け付けました");
                state.notifyListeners();
                return true;
            });
    }

    // --- サーバーからの自発的なブロードキャストハンドラ ---

    public void onDistributeVoteResult(JsonNode node) {
        // リセット
        state.lastVoteTieCandidates.clear();
        state.lastVoteTopCount = 0;

        log("[システム] 投票が終了しました。");

        JsonNode countsNode = node.get("voteCounts");
        if (countsNode != null && countsNode.isObject()) {
            int max = 0;
            for (var it = countsNode.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                int cnt = countsNode.get(name).asInt();
                if (cnt > max) max = cnt;
            }
            java.util.List<String> top = new java.util.ArrayList<>();
            for (var it = countsNode.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                int cnt = countsNode.get(name).asInt();
                if (cnt == max) top.add(name);
            }

            if (top.size() > 1) {
                // 同票のメッセージ形式
                StringJoiner sj = new StringJoiner(" と ");
                for (String p : top) sj.add("[" + p + "]");
                log("[システム] 投票の結果、" + sj.toString() + " が同票（" + max + "票）で最多となりました。");
                // 抽選開始メッセージ
                log("[システム] ランダム抽選を行います……");

                state.lastVoteTieCandidates.addAll(top);
                state.lastVoteTopCount = max;
            } else if (top.size() == 1) {
                String target = top.get(0);
                log("[投票集計] 最多票: " + target + "（" + max + "票）");
            }
        } else {
            String target = node.get("targetName").asText();
            log("[投票集計] 最多票: " + target);
        }

        state.notifyListeners();
    }

    private void log(String msg) {
        state.chatLog.add(msg);
    }
}
