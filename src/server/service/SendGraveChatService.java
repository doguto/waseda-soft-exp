package src.server.service;

import src.message.ChatBroadcastMessage;
import src.message.SendChatResultMessage;
import src.message.SendGraveChatMessage;
import src.server.core.Broadcaster;
import src.server.database.entity.ChatMessage;
import src.server.game.GameMaster;

public class SendGraveChatService extends BaseService {
    private final Broadcaster broadcaster;

    public SendGraveChatService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public SendChatResultMessage call(SendGraveChatMessage msg) {
        if (gameMaster.playerRepository.isAlive(msg.senderName)) {
            return new SendChatResultMessage(false);
        }
        
        gameMaster.chatRepository.addGraveMessage(new ChatMessage(msg.senderName, msg.text));
        broadcaster.broadcastDead(msg.roomId, new ChatBroadcastMessage("GRAVE", msg.senderName, msg.text));
        return new SendChatResultMessage(true);
    }
}
