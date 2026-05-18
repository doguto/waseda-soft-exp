package src.client.network;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;

public class ServerConnection implements Closeable {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ObjectMapper mapper = new ObjectMapper();

    public ServerConnection(String host, int port) throws IOException {
        socket = new Socket(InetAddress.getByName(host), port);
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send(Object message) throws Exception {
        out.println(mapper.writeValueAsString(message));
    }

    public BufferedReader getReader() {
        return in;
    }

    @Override
    public void close() throws IOException {
        out.println("END");
        socket.close();
    }
}
