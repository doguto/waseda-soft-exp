package src.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.message.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * 人狼ゲームのデバッグ用CLIクライアント。
 * 受信は別スレッドで常時監視し、送信はメインスレッドのメニューで操作する。
 */
public class DebugClient {

    private static String roomId = "room1";
    private static String playerId = "player1";
    private static String playerName = "デバッグ太郎";
    private static PrintWriter out;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("playerId [player1]: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) playerId = input;

        System.out.print("playerName [デバッグ太郎]: ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) playerName = input;

        System.out.print("roomId [room1]: ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) roomId = input;

        Socket socket = new Socket(InetAddress.getByName("localhost"), 8080);
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 受信スレッド：サーバーからのブロードキャストも拾う
        Thread receiver = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        JsonNode node = mapper.readTree(line);
                        String type = node.has("message_type") ? node.get("message_type").asText() : "?";
                        System.out.println("\n[受信:" + type + "] " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
                        printPrompt();
                    } catch (Exception e) {
                        System.out.println("[受信RAW] " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("[切断]");
            }
        });
        receiver.setDaemon(true);
        receiver.start();

        System.out.println("接続完了: playerId=" + playerId + " roomId=" + roomId);
        printMenu();

        while (true) {
            printPrompt();
            String cmd = scanner.nextLine().trim();
            switch (cmd) {
                case "1" -> send(createRoom());
                case "2" -> send(joinRoom());
                case "3" -> send(startGame());
                case "4" -> {
                    System.out.print("  targetId: ");
                    String target = scanner.nextLine().trim();
                    send(vote(target));
                }
                case "5" -> {
                    System.out.print("  テキスト: ");
                    String text = scanner.nextLine().trim();
                    send(villageChat(text));
                }
                case "6" -> {
                    System.out.print("  テキスト(狼チャット): ");
                    String text = scanner.nextLine().trim();
                    send(wolfChat(text));
                }
                case "7" -> {
                    System.out.print("  targetId(狼が襲う): ");
                    String target = scanner.nextLine().trim();
                    send(wolfAttack(target));
                }
                case "8" -> {
                    System.out.print("  targetId(占い対象): ");
                    String target = scanner.nextLine().trim();
                    send(seerInvestigate(target));
                }
                case "9" -> {
                    System.out.print("  targetId(守る対象): ");
                    String target = scanner.nextLine().trim();
                    send(knightGuard(target));
                }
                case "10" -> send(endDiscussion());
                case "11" -> {
                    System.out.print("  JSONを直接入力: ");
                    String raw = scanner.nextLine().trim();
                    out.println(raw);
                }
                case "m" -> printMenu();
                case "q" -> {
                    out.println("END");
                    socket.close();
                    System.out.println("終了");
                    return;
                }
                default -> System.out.println("不明なコマンド。'm' でメニュー表示");
            }
        }
    }

    private static void send(Object msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        System.out.println("[送信] " + json);
        out.println(json);
    }

    private static void printPrompt() {
        System.out.print("> ");
    }

    private static void printMenu() {
        System.out.println("""
            ── メニュー ─────────────────────────────
             1) create_room     2) join_room
             3) start_game      4) vote (targetId)
             5) village_chat    6) wolf_chat
             7) wolf_attack     8) seer_investigate
             9) knight_guard   10) end_discussion
            11) RAW JSON送信
             m) メニュー再表示  q) 終了
            ─────────────────────────────────────────""");
    }

    // ── メッセージファクトリ ──────────────────────────────

    private static CreateRoomMessage createRoom() {
        var m = new CreateRoomMessage();
        m.roomId = roomId; m.playerId = playerId; m.name = playerName;
        return m;
    }

    private static JoinRoomMessage joinRoom() {
        var m = new JoinRoomMessage();
        m.roomId = roomId; m.playerId = playerId; m.name = playerName;
        return m;
    }

    private static StartGameMessage startGame() {
        var m = new StartGameMessage();
        m.roomId = roomId; m.playerId = playerId;
        return m;
    }

    private static VoteMessage vote(String targetId) {
        var m = new VoteMessage();
        m.roomId = roomId; m.playerId = playerId; m.targetId = targetId;
        return m;
    }

    private static SendVillageChatMessage villageChat(String text) {
        var m = new SendVillageChatMessage();
        m.roomId = roomId; m.senderId = playerId; m.text = text;
        return m;
    }

    private static SendWolfChatMessage wolfChat(String text) {
        var m = new SendWolfChatMessage();
        m.roomId = roomId; m.senderId = playerId; m.text = text;
        return m;
    }

    private static WolfAttackMessage wolfAttack(String targetId) {
        var m = new WolfAttackMessage();
        m.roomId = roomId; m.wolfId = playerId; m.targetId = targetId;
        return m;
    }

    private static SeerInvestigateMessage seerInvestigate(String targetId) {
        var m = new SeerInvestigateMessage();
        m.roomId = roomId; m.seerId = playerId; m.targetId = targetId;
        return m;
    }

    private static KnightGuardMessage knightGuard(String targetId) {
        var m = new KnightGuardMessage();
        m.roomId = roomId; m.knightId = playerId; m.targetId = targetId;
        return m;
    }

    private static EndDiscussionMessage endDiscussion() {
        var m = new EndDiscussionMessage();
        m.roomId = roomId; m.playerId = playerId;
        return m;
    }
}
