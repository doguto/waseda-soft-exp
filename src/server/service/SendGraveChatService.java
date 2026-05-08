package src.server.service;

import src.message.ChatBroadcastMessage;
import src.message.SendChatResultMessage;
import src.message.SendGraveChatMessage;
import src.server.Broadcaster;
import src.server.GameMaster;
import src.server.database.entity.ChatMessage;
import src.server.database.repository.ChatRepository;
import src.server.database.repository.PlayerRepository;

public class SendGraveChatService extends BaseService {
    private final ChatRepository chatRepo = new ChatRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public SendGraveChatService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public SendChatResultMessage call(SendGraveChatMessage msg) {
        String senderName = playerRepo.findById(msg.roomId, msg.senderId)
            .map(p -> p.name).orElse("?");
        chatRepo.addGraveMessage(msg.roomId, new ChatMessage(msg.senderId, senderName, msg.text));
        broadcaster.broadcastDead(msg.roomId,
            new ChatBroadcastMessage("GRAVE", msg.senderId, senderName, msg.text));
        return new SendChatResultMessage(true);
    }
}
