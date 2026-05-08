package src.server.service;

import src.message.ChatBroadcastMessage;
import src.message.SendChatResultMessage;
import src.message.SendVillageChatMessage;
import src.server.Broadcaster;
import src.server.GameMaster;
import src.server.database.entity.ChatMessage;
import src.server.database.repository.ChatRepository;
import src.server.database.repository.PlayerRepository;

public class SendVillageChatService extends BaseService {
    private final ChatRepository chatRepo = new ChatRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public SendVillageChatService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public SendChatResultMessage call(SendVillageChatMessage msg) {
        String senderName = playerRepo.findById(msg.roomId, msg.senderId)
            .map(p -> p.name).orElse("?");
        chatRepo.addVillageMessage(msg.roomId, new ChatMessage(msg.senderId, senderName, msg.text));
        broadcaster.broadcastAlive(msg.roomId,
            new ChatBroadcastMessage("VILLAGE", msg.senderId, senderName, msg.text));
        return new SendChatResultMessage(true);
    }
}
