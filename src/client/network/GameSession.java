package src.client.network;

public class GameSession {
    private ServerConnection connection;

    public void setConnection(ServerConnection conn) {
        this.connection = conn;
    }

    public ServerConnection getConnection() {
        return connection;
    }

    public void send(Object msg) {
        if (connection == null) return;
        try {
            connection.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
