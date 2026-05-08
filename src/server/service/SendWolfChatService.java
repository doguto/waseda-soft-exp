package src.server.service;

import src.message.ChatBroadcastMessage;
import src.message.SendChatResultMessage;
import src.message.SendWolfChatMessage;
import src.server.Broadcaster;
import src.server.GameMaster;
import src.server.database.entity.ChatMessage;
import src.server.database.entity.Role;
import src.server.database.repository.ChatRepository;
import src.server.database.repository.PlayerRepository;

public class SendWolfChatService extends BaseService {
    private final ChatRepository chatRepo = new ChatRepository();
    private final PlayerRepository playerRepo = new PlayerRepository();
    private final Broadcaster broadcaster;

    public SendWolfChatService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public SendChatResultMessage call(SendWolfChatMessage msg) {
        String senderName = playerRepo.findById(msg.roomId, msg.senderId)
            .map(p -> p.name).orElse("?");
        chatRepo.addWolfMessage(msg.roomId, new ChatMessage(msg.senderId, senderName, msg.text));
        broadcaster.broadcastToRole(msg.roomId, Role.WOLF,
            new ChatBroadcastMessage("WOLF", msg.senderId, senderName, msg.text));
        return new SendChatResultMessage(true);
    }
}
