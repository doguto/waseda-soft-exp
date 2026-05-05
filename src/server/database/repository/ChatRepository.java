package src.server.database.repository;

import src.server.database.GameDatabase;
import src.server.database.RoomData;
import src.server.database.entity.ChatMessage;

import java.util.List;

public class ChatRepository {
    private final GameDatabase db = GameDatabase.getInstance();

    public void addVillageMessage(String roomId, ChatMessage message) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.villageChat.add(message);
    }

    public void addWolfMessage(String roomId, ChatMessage message) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.wolfChat.add(message);
    }

    public void addGraveMessage(String roomId, ChatMessage message) {
        RoomData room = db.getRoom(roomId);
        if (room != null) room.graveChat.add(message);
    }

    public List<ChatMessage> getVillageMessages(String roomId) {
        RoomData room = db.getRoom(roomId);
        return room != null ? room.villageChat : List.of();
    }

    public List<ChatMessage> getWolfMessages(String roomId) {
        RoomData room = db.getRoom(roomId);
        return room != null ? room.wolfChat : List.of();
    }

    public List<ChatMessage> getGraveMessages(String roomId) {
        RoomData room = db.getRoom(roomId);
        return room != null ? room.graveChat : List.of();
    }
}
