package src.client.presenter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.message.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MessageDispatcher {
    private final RoomPresenter roomPresenter;
    private final NoonActionPresenter noonPresenter;
    private final NightActionPresenter nightPresenter;
    private final ChatPresenter chatPresenter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Consumer<JsonNode>> handlers = new HashMap<>();

    public MessageDispatcher(RoomPresenter roomPresenter, NoonActionPresenter noonPresenter,
                             NightActionPresenter nightPresenter, ChatPresenter chatPresenter) {
        this.roomPresenter  = roomPresenter;
        this.noonPresenter  = noonPresenter;
        this.nightPresenter = nightPresenter;
        this.chatPresenter  = chatPresenter;
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put(CreateRoomResultMessage.MessageType,     roomPresenter::onCreateRoomResult);
        handlers.put(JoinRoomResultMessage.MessageType,       roomPresenter::onJoinRoomResult);
        handlers.put(StartGameResultMessage.MessageType,      roomPresenter::onStartGameResult);
        handlers.put(DistributeRoleMessage.MessageType,       roomPresenter::onDistributeRole);
        handlers.put(AnnounceMorningMessage.MessageType,      roomPresenter::onAnnounceMorning);
        handlers.put(ExecuteMessage.MessageType,              roomPresenter::onExecute);
        handlers.put(AnnounceGameOverMessage.MessageType,     roomPresenter::onAnnounceGameOver);

        handlers.put(EndDiscussionResultMessage.MessageType,  noonPresenter::onEndDiscussionResult);
        handlers.put(VoteResultMessage.MessageType,           noonPresenter::onVoteResult);
        handlers.put(DistributeVoteResultMessage.MessageType, noonPresenter::onDistributeVoteResult);

        handlers.put(SeerResultMessage.MessageType,           nightPresenter::onSeerResult);
        handlers.put(MediumResultMessage.MessageType,         nightPresenter::onMediumResult);
        handlers.put(WolfAttackResultMessage.MessageType,     nightPresenter::onWolfAttackResult);
        handlers.put(KnightGuardResultMessage.MessageType,    nightPresenter::onKnightGuardResult);
        handlers.put(SeerInvestigateMessage.MessageType,      nightPresenter::onSeerInvestigateResult);

        handlers.put(ChatBroadcastMessage.MessageType,        chatPresenter::onChatBroadcast);
    }

    public void dispatch(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            String type = node.has("message_type") ? node.get("message_type").asText() : null;
            if (type == null) return;
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
