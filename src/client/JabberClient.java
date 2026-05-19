package src.client;

import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.message.HelloRequestMessage;
import src.message.HelloResponseMessage;

public class JabberClient { 
    public static void main(String[] args) 
    throws IOException { 
        InetAddress addr = InetAddress.getByName("localhost"); // IP アドレスへの変換 
        System.out.println("addr = " + addr); 
        Socket socket = new Socket(addr, 8080); // ソケットの生成
        ObjectMapper mapper = new ObjectMapper();
        try { 
            System.out.println("socket = " + socket); 
            BufferedReader in = new BufferedReader( 
                new InputStreamReader(socket.getInputStream())
            ); // データ受信用バッファの設定 
            PrintWriter out = new PrintWriter( 
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                true
            ); // 送信バッファ設定 
            for(int i = 0; i < 10; i++) { 
                HelloRequestMessage request = new HelloRequestMessage("howdy " + i);
                String jsonRequest = mapper.writeValueAsString(request);
                out.println(jsonRequest); // データ送信 

                String str = in.readLine(); // データ受信 
                HelloResponseMessage response = mapper.readValue(str, HelloResponseMessage.class);
                System.out.println("Received: " + response.greeting); 
            } 
            out.println("END"); 
        } finally { 
            System.out.println("closing..."); 
            socket.close(); 
        } 
    } 
}