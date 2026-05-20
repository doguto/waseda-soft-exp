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
        try {
            System.out.println("[BROADCAST] room=" + roomId + " type=" + message.getClass().getSimpleName());
            registry.broadcastToRoom(roomId, mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.out.println("[ERROR] broadcast failed: room=" + roomId + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendTo(String playerName, Object message) {
        try {
            System.out.println("[SEND] to=" + playerName + " type=" + message.getClass().getSimpleName());
            registry.sendTo(playerName, mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.out.println("[ERROR] sendTo failed: player=" + playerName + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastAlive(String roomId, Object message) {
        try {
            System.out.println("[BROADCAST_ALIVE] room=" + roomId + " type=" + message.getClass().getSimpleName());
            registry.broadcastAlive(roomId, mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.out.println("[ERROR] broadcastAlive failed: room=" + roomId + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastToRole(String roomId, Role role, Object message) {
        try {
            System.out.println("[BROADCAST_ROLE] room=" + roomId + " role=" + role + " type=" + message.getClass().getSimpleName());
            registry.broadcastToRole(roomId, role, mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.out.println("[ERROR] broadcastToRole failed: room=" + roomId + " role=" + role + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastDead(String roomId, Object message) {
        try {
            System.out.println("[BROADCAST_DEAD] room=" + roomId + " type=" + message.getClass().getSimpleName());
            registry.broadcastDead(roomId, mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.out.println("[ERROR] broadcastDead failed: room=" + roomId + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── server entry point ───────────────────────────────────────────────────

    public void start() throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[INFO] Server started on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[INFO] Accepted connection from: " + socket.getRemoteSocketAddress());
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
            System.out.println("[WARN] Client connection closed: " + e.getMessage());
        } finally {
            String name = connectedPlayerName[0];
            if (name != null) {
                registry.remove(name);
                System.out.println("[INFO] Player disconnected: " + name);
            }
        }
    }

    // リクエストを対応するServiceクラスにルーティングして処理する
    private String route(String json, PrintWriter out, String[] connectedPlayerName) {
        JsonNode node;
        try {
            node = mapper.readTree(json);
        } catch (Exception e) {
            System.out.println("[ERROR] JSON parse failed: " + e.getMessage());
            return null;
        }

        String type = node.has("message_type") ? node.get("message_type").asText() : null;
        if (type == null) {
            System.out.println("[WARN] Missing message_type in request");
            return null;
        }

        System.out.println("[INFO] Received: " + type);

        return switch (type) {
            case HelloRequestMessage.MessageType -> {
                try {
                    HelloRequestMessage req = mapper.readValue(json, HelloRequestMessage.class);
                    yield mapper.writeValueAsString(new HelloService().call(req));
                } catch (Exception e) {
                    System.out.println("[ERROR] HelloService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case CreateRoomMessage.MessageType -> {
                try {
                    CreateRoomMessage msg = mapper.readValue(json, CreateRoomMessage.class);
                    System.out.println("[INFO] CreateRoom: roomId=" + msg.roomId + " name=" + msg.name);
                    GameMaster gm = gameMasters.computeIfAbsent(msg.roomId, GameMaster::new);
                    registry.register(msg.name, out);
                    registry.joinRoom(msg.roomId, msg.name);
                    connectedPlayerName[0] = msg.name;
                    CreateRoomResultMessage res = new CreateRoomService(msg.roomId, gm).call(msg);
                    yield mapper.writeValueAsString(res);
                } catch (Exception e) {
                    System.out.println("[ERROR] CreateRoomService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case JoinRoomMessage.MessageType -> {
                try {
                    JoinRoomMessage msg = mapper.readValue(json, JoinRoomMessage.class);
                    System.out.println("[INFO] JoinRoom: roomId=" + msg.roomId + " name=" + msg.name);
                    GameMaster gm = gameMasters.computeIfAbsent(msg.roomId, GameMaster::new);
                    registry.register(msg.name, out);
                    registry.joinRoom(msg.roomId, msg.name);
                    connectedPlayerName[0] = msg.name;
                    yield mapper.writeValueAsString(new JoinRoomService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] JoinRoomService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case DeleteRoomMessage.MessageType -> {
                try {
                    DeleteRoomMessage msg = mapper.readValue(json, DeleteRoomMessage.class);
                    System.out.println("[INFO] DeleteRoom: roomId=" + msg.roomId);
                    GameMaster gm = gameMasters.remove(msg.roomId);
                    if (gm == null) yield mapper.writeValueAsString(new DeleteRoomResultMessage(false));
                    yield mapper.writeValueAsString(new DeleteRoomService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] DeleteRoomService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case StartGameMessage.MessageType -> {
                try {
                    StartGameMessage msg = mapper.readValue(json, StartGameMessage.class);
                    System.out.println("[INFO] StartGame: roomId=" + msg.roomId);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    if (gm == null) yield mapper.writeValueAsString(new StartGameResultMessage(false, "ルームが存在しません"));
                    yield mapper.writeValueAsString(new StartGameService(msg.roomId, gm, this).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] StartGameService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case WolfAttackMessage.MessageType -> {
                try {
                    WolfAttackMessage msg = mapper.readValue(json, WolfAttackMessage.class);
                    System.out.println("[INFO] WolfAttack: roomId=" + msg.roomId + " wolf=" + msg.wolfName + " target=" + msg.targetName);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new WolfAttackService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] WolfAttackService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case SeerInvestigateMessage.MessageType -> {
                try {
                    SeerInvestigateMessage msg = mapper.readValue(json, SeerInvestigateMessage.class);
                    System.out.println("[INFO] SeerInvestigate: roomId=" + msg.roomId);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new SeerInvestigateService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] SeerInvestigateService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case KnightGuardMessage.MessageType -> {
                try {
                    KnightGuardMessage msg = mapper.readValue(json, KnightGuardMessage.class);
                    System.out.println("[INFO] KnightGuard: roomId=" + msg.roomId);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new KnightGuardService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] KnightGuardService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case EndDiscussionMessage.MessageType -> {
                try {
                    EndDiscussionMessage msg = mapper.readValue(json, EndDiscussionMessage.class);
                    System.out.println("[INFO] EndDiscussion: roomId=" + msg.roomId);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new EndDiscussionService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] EndDiscussionService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case VoteMessage.MessageType -> {
                try {
                    VoteMessage msg = mapper.readValue(json, VoteMessage.class);
                    System.out.println("[INFO] Vote: roomId=" + msg.roomId + " player=" + msg.playerName + " target=" + msg.targetName);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new VoteService(msg.roomId, gm).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] VoteService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case SendVillageChatMessage.MessageType -> {
                try {
                    SendVillageChatMessage msg = mapper.readValue(json, SendVillageChatMessage.class);
                    System.out.println("[INFO] VillageChat: roomId=" + msg.roomId + " sender=" + msg.senderName);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new SendVillageChatService(msg.roomId, gm, this).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] SendVillageChatService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case SendWolfChatMessage.MessageType -> {
                try {
                    SendWolfChatMessage msg = mapper.readValue(json, SendWolfChatMessage.class);
                    System.out.println("[INFO] WolfChat: roomId=" + msg.roomId + " sender=" + msg.senderName);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new SendWolfChatService(msg.roomId, gm, this).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] SendWolfChatService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            case SendGraveChatMessage.MessageType -> {
                try {
                    SendGraveChatMessage msg = mapper.readValue(json, SendGraveChatMessage.class);
                    System.out.println("[INFO] GraveChat: roomId=" + msg.roomId + " sender=" + msg.senderName);
                    GameMaster gm = gameMasters.get(msg.roomId);
                    yield mapper.writeValueAsString(new SendGraveChatService(msg.roomId, gm, this).call(msg));
                } catch (Exception e) {
                    System.out.println("[ERROR] SendGraveChatService failed: " + e.getMessage());
                    e.printStackTrace();
                    yield null;
                }
            }
            default -> {
                System.out.println("[WARN] Unsupported message type: " + type);
                yield null;
            }
        };
    }
}
