package src.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.message.*;
import src.server.core.Broadcaster;
import src.server.core.ClientRegistry;
import src.server.game.GameMaster;
import src.server.service.*;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class Router {
    private final ObjectMapper mapper;
    private final ClientRegistry registry;
    private final Map<String, GameMaster> gameMasters;
    private final Broadcaster broadcaster;

    public Router(ObjectMapper mapper, ClientRegistry registry, Map<String, GameMaster> gameMasters, Broadcaster broadcaster) {
        this.mapper = mapper;
        this.registry = registry;
        this.gameMasters = gameMasters;
        this.broadcaster = broadcaster;
    }

    public String route(String json, PrintWriter out, String[] connectedPlayerName) {
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
                    yield mapper.writeValueAsString(new CreateRoomService(msg.roomId, gm).call(msg));
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
                    if (gm == null) yield mapper.writeValueAsString(new StartGameResultMessage(false, "ルームが存在しません", List.of()));
                    yield mapper.writeValueAsString(new StartGameService(msg.roomId, gm, broadcaster).call(msg));
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
                    String requester = connectedPlayerName[0];
                    yield mapper.writeValueAsString(new EndDiscussionService(msg.roomId, gm).call(msg, requester));
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
                    yield mapper.writeValueAsString(new SendVillageChatService(msg.roomId, gm, broadcaster).call(msg));
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
                    yield mapper.writeValueAsString(new SendWolfChatService(msg.roomId, gm, broadcaster).call(msg));
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
                    yield mapper.writeValueAsString(new SendGraveChatService(msg.roomId, gm, broadcaster).call(msg));
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
