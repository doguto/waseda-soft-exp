package src.server;
import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import src.message.HelloRequestMessage;
import src.message.HelloResponseMessage;
import src.server.service.HelloService;

public class JabberServer { 
    public static final int PORT = 8080; // ポート番号を設定する． 
    public static void main(String[] args) 
    throws IOException { 
        ServerSocket s = new ServerSocket(PORT); // ソケットを作成する 
        System.out.println("Started: " + s); 
        ObjectMapper mapper = new ObjectMapper();
        try { 
            Socket socket = s.accept(); // コネクション設定要求を待つ 
            try { 
                System.out.println("Connection accepted: " + socket); 
                BufferedReader in = new BufferedReader( 
                    new InputStreamReader(socket.getInputStream())
                ); // データ受信用バッファの設定 
                PrintWriter out = new PrintWriter( 
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                    true
                ); // 送信バッファ設定 
                HelloService helloService = new HelloService();
                while (true) {
                    String str = in.readLine(); // データの受信
                    if(str == null || str.equals("END")) break;

                    JsonNode node = mapper.readTree(str);
                    String messageType = node.has("message_type") ? node.get("message_type").asText() : null;
                    
                    if (messageType == null) {
                        System.out.println("Unknown message format: " + str);
                        continue;
                    }

                    switch (messageType) {
                        case HelloRequestMessage.MessageType:
                            HelloRequestMessage requestMsg = mapper.readValue(str, HelloRequestMessage.class);
                            HelloResponseMessage responseMsg = helloService.call(requestMsg);
                            
                            String jsonResponse = mapper.writeValueAsString(responseMsg);
                            System.out.println("Responding: " + jsonResponse);
                            out.println(jsonResponse);  // データの送信
                            break;
                        default:
                            System.out.println("Unsupported message type: " + messageType);
                            break;
                    }
                }
            } finally { 
                System.out.println("closing..."); 
                socket.close(); 
            } 
        } finally { 
            s.close(); 
        } 
    } 
}
