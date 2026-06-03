package src.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import src.server.core.Broadcaster;
import src.server.core.ClientRegistry;
import src.server.database.GameDatabase;
import src.common.Role;
import src.server.game.GameMaster;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

public class JabberServer implements Broadcaster {
    public static final int PORT = 8080;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ClientRegistry registry = new ClientRegistry();
    private final Map<String, GameMaster> gameMasters = new ConcurrentHashMap<>();
    private Router router;

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
        router = new Router(mapper, registry, gameMasters, this);
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
                // 退出メッセージをルームに保存して全員へ broadcast
                String roomId = registry.findRoomOfPlayer(name);
                if (roomId != null) {
                    GameMaster gm = gameMasters.get(roomId);
                    if (gm != null) {
                        try {
                            if (gm.getStateManager().getCurrentPhase() == src.common.GamePhase.WAITING) {
                                new src.server.database.repository.RoomRepository().removePlayer(roomId, name);
                            }
                            src.message.SendVillageChatMessage cm = new src.message.SendVillageChatMessage();
                            cm.roomId = roomId;
                            cm.senderName = "システム";
                            cm.text = name + " が退出しました";
                            new src.server.service.SendVillageChatService(roomId, gm, this).call(cm);
                        } catch (Exception ex) {
                            System.out.println("[WARN] failed to send leave system message: " + ex.getMessage());
                        }
                    }
                }
                registry.remove(name);
                System.out.println("[INFO] Player disconnected: " + name);
            }
        }
    }

    // リクエストを Router に委譲してルーティングする
    private String route(String json, PrintWriter out, String[] connectedPlayerName) {
        return router.route(json, out, connectedPlayerName);
    }
}
