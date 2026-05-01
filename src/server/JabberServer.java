package src.server;
import java.io.*;
import java.net.*;
import src.message.HelloRequestMessage;
import src.message.HelloResponseMessage;
import src.server.service.HelloService;

public class JabberServer { 
    public static final int PORT = 8080; // ポート番号を設定する． 
    public static void main(String[] args) 
    throws IOException { 
        ServerSocket s = new ServerSocket(PORT); // ソケットを作成する 
        System.out.println("Started: " + s); 
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
                    if(str.equals("END")) break;

                    HelloRequestMessage request = new HelloRequestMessage(str);
                    HelloResponseMessage response = helloService.call(request);
                    System.out.println("Responding: " + response.greeting);
                    out.println(response.greeting);  // データの送信
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
