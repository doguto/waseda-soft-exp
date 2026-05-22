package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.message.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessageDispatcher {
    private final RoomPresenter roomPresenter;
    private final NoonActionPresenter noonPresenter;
    private final NightActionPresenter nightPresenter;
    private final ChatPresenter chatPresenter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Consumer<JsonNode>> handlers = new HashMap<>();
    // Request-Response 用: 一回限りの応答待ち Future
    private final ConcurrentHashMap<String, CompletableFuture<JsonNode>> pendingFutures = new ConcurrentHashMap<>();

    public MessageDispatcher(RoomPresenter roomPresenter, NoonActionPresenter noonPresenter,
                             NightActionPresenter nightPresenter, ChatPresenter chatPresenter) {
        this.roomPresenter  = roomPresenter;
        this.noonPresenter  = noonPresenter;
        this.nightPresenter = nightPresenter;
        this.chatPresenter  = chatPresenter;
        registerHandlers();
    }

    /**
     * 指定した message_type の応答を一回だけ待つ Future を登録して返す。
     * sendRequest() 呼び出し前に登録することで受信漏れを防ぐ。
     */
    public CompletableFuture<JsonNode> expectResponse(String responseType) {
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        pendingFutures.put(responseType, future);
        return future;
    }

    private void registerHandlers() {
        // --- サーバーからの自発的なブロードキャスト ---
        handlers.put(DistributeRoleMessage.MessageType,       roomPresenter::onDistributeRole);
        handlers.put(AnnounceMorningMessage.MessageType,      roomPresenter::onAnnounceMorning);
        handlers.put(ExecuteMessage.MessageType,              roomPresenter::onExecute);
        handlers.put(AnnounceGameOverMessage.MessageType,     roomPresenter::onAnnounceGameOver);

        handlers.put(DistributeVoteResultMessage.MessageType, noonPresenter::onDistributeVoteResult);

        handlers.put(MediumResultMessage.MessageType,         nightPresenter::onMediumResult);

        handlers.put(ChatBroadcastMessage.MessageType,        chatPresenter::onChatBroadcast);
        // Request-Response 系 (CreateRoomResult, JoinRoomResult, StartGameResult,
        // EndDiscussionResult, VoteResult, WolfAttackResult, KnightGuardResult,
        // SeerResult) は pendingFutures 経由で処理する。
    }

    public void dispatch(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            String type = node.has("message_type") ? node.get("message_type").asText() : null;
            if (type == null) return;

            // pending future があればそちらを優先して complete (一回限り)
            CompletableFuture<JsonNode> pending = pendingFutures.remove(type);
            if (pending != null) {
                pending.complete(node);
                return;
            }

            Consumer<JsonNode> handler = handlers.get(type);
            if (handler != null) {
                handler.accept(node);
            } else {
                System.out.println("[システム] 未処理メッセージ: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisconnect() {
        roomPresenter.onDisconnect();
    }
}
