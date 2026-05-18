package src.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.message.*;
import src.server.core.Broadcaster;
import src.server.core.ClientRegistry;
import src.server.database.entity.Role;
import src.server.game.GameMaster;
import src.server.service.*;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;
import src.server.database.repository.RoomRepository;

public class JabberServer implements Broadcaster {
    public static final int PORT = 8080;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ClientRegistry registry = new ClientRegistry();
    private final Map<String, GameMaster> gameMasters = new ConcurrentHashMap<>();
    private final RoomRepository roomRepository = new RoomRepository();

    // ── Broadcaster implementation ───────────────────────────────────────────

    @Override
    public void broadcast(String roomId, Object message) {
        try { registry.broadcastToRoom(roomId, mapper.writeValueAsString(message)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void sendTo(String playerName, Object message) {
        try { registry.sendTo(playerName, mapper.writeValueAsString(message)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void broadcastAlive(String roomId, Object message) {
        try { registry.broadcastAlive(roomId, mapper.writeValueAsString(message)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void broadcastToRole(String roomId, Role role, Object message) {
        try { registry.broadcastToRole(roomId, role, mapper.writeValueAsString(message)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void broadcastDead(String roomId, Object message) {
        try { registry.broadcastDead(roomId, mapper.writeValueAsString(message)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // ── server entry point ───────────────────────────────────────────────────

    public void start() throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Started: " + serverSocket);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(() -> handleClient(socket));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new JabberServer().start();
    }

    // ── client handler (one thread per connection) ───────────────────────────

    private void handleClient(Socket socket) {
        String[] connectedPlayerName = {null};
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END")) break;
                String response = route(line, out, connectedPlayerName);
                if (response != null) out.println(response);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            if (connectedPlayerName[0] != null) registry.remove(connectedPlayerName[0]);
        }
    }

    // リクエストを対応するServiceクラスにルーティングして処理する
    private String route(String json, PrintWriter out, String[] connectedPlayerName) throws Exception {
        JsonNode node = mapper.readTree(json);
        String type = node.has("message_type") ? node.get("message_type").asText() : null;
        if (type == null) return null;

        return switch (type) {
            case HelloRequestMessage.MessageType -> {
                HelloRequestMessage req = mapper.readValue(json, HelloRequestMessage.class);
                yield mapper.writeValueAsString(new HelloService().call(req));
            }
            case CreateRoomMessage.MessageType -> {
                CreateRoomMessage msg = mapper.readValue(json, CreateRoomMessage.class);
                GameMaster gm = gameMasters.computeIfAbsent(msg.roomId, GameMaster::new);
                registry.register(msg.name, out);
                registry.joinRoom(msg.roomId, msg.name);
                connectedPlayerName[0] = msg.name;
                CreateRoomResultMessage res = new CreateRoomService(msg.roomId, gm, roomRepository).call(msg);
                yield mapper.writeValueAsString(res);
            }
            case JoinRoomMessage.MessageType -> {
                JoinRoomMessage msg = mapper.readValue(json, JoinRoomMessage.class);
                GameMaster gm = gameMasters.computeIfAbsent(msg.roomId, GameMaster::new);
                registry.register(msg.name, out);
                registry.joinRoom(msg.roomId, msg.name);
                connectedPlayerName[0] = msg.name;
                yield mapper.writeValueAsString(new JoinRoomService(msg.roomId, gm, roomRepository).call(msg));
            }
            case DeleteRoomMessage.MessageType -> {
                DeleteRoomMessage msg = mapper.readValue(json, DeleteRoomMessage.class);
                GameMaster gm = gameMasters.remove(msg.roomId);
                if (gm == null) yield mapper.writeValueAsString(new DeleteRoomResultMessage(false));
                yield mapper.writeValueAsString(new DeleteRoomService(msg.roomId, gm, roomRepository).call(msg));
            }
            case StartGameMessage.MessageType -> {
                StartGameMessage msg = mapper.readValue(json, StartGameMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                if (gm == null) yield mapper.writeValueAsString(new StartGameResultMessage(false, "ルームが存在しません"));
                yield mapper.writeValueAsString(new StartGameService(msg.roomId, gm, this).call(msg));
            }
            case WolfAttackMessage.MessageType -> {
                WolfAttackMessage msg = mapper.readValue(json, WolfAttackMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new WolfAttackService(msg.roomId, gm).call(msg));
            }
            case SeerInvestigateMessage.MessageType -> {
                SeerInvestigateMessage msg = mapper.readValue(json, SeerInvestigateMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new SeerInvestigateService(msg.roomId, gm).call(msg));
            }
            case KnightGuardMessage.MessageType -> {
                KnightGuardMessage msg = mapper.readValue(json, KnightGuardMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new KnightGuardService(msg.roomId, gm).call(msg));
            }
            case EndDiscussionMessage.MessageType -> {
                EndDiscussionMessage msg = mapper.readValue(json, EndDiscussionMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new EndDiscussionService(msg.roomId, gm).call(msg));
            }
            case VoteMessage.MessageType -> {
                VoteMessage msg = mapper.readValue(json, VoteMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new VoteService(msg.roomId, gm).call(msg));
            }
            case SendVillageChatMessage.MessageType -> {
                SendVillageChatMessage msg = mapper.readValue(json, SendVillageChatMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new SendVillageChatService(msg.roomId, gm, this).call(msg));
            }
            case SendWolfChatMessage.MessageType -> {
                SendWolfChatMessage msg = mapper.readValue(json, SendWolfChatMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new SendWolfChatService(msg.roomId, gm, this).call(msg));
            }
            case SendGraveChatMessage.MessageType -> {
                SendGraveChatMessage msg = mapper.readValue(json, SendGraveChatMessage.class);
                GameMaster gm = gameMasters.get(msg.roomId);
                yield mapper.writeValueAsString(new SendGraveChatService(msg.roomId, gm, this).call(msg));
            }
            default -> {
                System.out.println("Unsupported message type: " + type);
                yield null;
            }
        };
    }
}
