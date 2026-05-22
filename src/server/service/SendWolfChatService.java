package src.server.service;

import src.message.ChatBroadcastMessage;
import src.message.SendChatResultMessage;
import src.message.SendWolfChatMessage;
import src.server.core.Broadcaster;
import src.server.database.entity.ChatMessage;
import src.server.database.entity.Role;
import src.server.game.GameMaster;

public class SendWolfChatService extends BaseService {
    private final Broadcaster broadcaster;

    public SendWolfChatService(String roomId, GameMaster gameMaster, Broadcaster broadcaster) {
        super(roomId, gameMaster);
        this.broadcaster = broadcaster;
    }

    public SendChatResultMessage call(SendWolfChatMessage msg) {
        if (gameMaster.playerRepository.getPlayerRole(msg.senderName) != Role.WOLF) {
            return new SendChatResultMessage(false);
        }

        gameMaster.chatRepository.addWolfMessage(new ChatMessage(msg.senderName, msg.text));
        broadcaster.broadcastToRole(msg.roomId, Role.WOLF,
            new ChatBroadcastMessage("WOLF", msg.senderName, msg.text));
        return new SendChatResultMessage(true);
    }
}
