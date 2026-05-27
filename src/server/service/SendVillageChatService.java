package src.server.service;

import src.message.ChatBroadcastMessage;
import src.message.SendChatResultMessage;
import src.message.SendVillageChatMessage;
import src.server.core.Broadcaster;
import src.server.database.entity.ChatMessage;
import src.server.game.GameMaster;

public class SendVillageChatService extends BaseService {
    private final Broadcaster broadcaster;

    public SendVillageChatService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public SendChatResultMessage call(SendVillageChatMessage msg) {
        if (!gameMaster.playerRepository.isAlive(msg.senderName)) {
            return new SendChatResultMessage(false);
        }
        gameMaster.chatRepository.addVillageMessage(new ChatMessage(msg.senderName, msg.text));
        broadcaster.broadcast(msg.roomId,
            new ChatBroadcastMessage("VILLAGE", msg.senderName, msg.text));
        return new SendChatResultMessage(true);
    }
}
