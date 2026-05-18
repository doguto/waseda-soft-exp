package src.client.network;

import src.client.dispatch.MessageDispatcher;

import java.io.IOException;

public class MessageReceiver {
    private final ServerConnection connection;
    private final MessageDispatcher dispatcher;

    public MessageReceiver(ServerConnection connection, MessageDispatcher dispatcher) {
        this.connection = connection;
        this.dispatcher = dispatcher;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            try {
                String line;
                while ((line = connection.getReader().readLine()) != null) {
                    dispatcher.dispatch(line);
                }
            } catch (IOException e) {
                dispatcher.onDisconnect();
            }
        }, "receiver");
        thread.setDaemon(true);
        thread.start();
    }
}
