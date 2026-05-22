package src.client.network;

import com.fasterxml.jackson.databind.JsonNode;
import src.client.presenter.MessageDispatcher;

import java.util.concurrent.CompletableFuture;

public class GameSession {
    private ServerConnection connection;
    private MessageDispatcher dispatcher;

    public void setConnection(ServerConnection conn) {
        this.connection = conn;
    }

    public ServerConnection getConnection() {
        return connection;
    }

    public void setDispatcher(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void send(Object msg) {
        if (connection == null) return;
        try {
            connection.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * リクエストを送信し、应答メッセージを非同期に待つ。
     * Future 登録後に send することで、応答が先に届いても漏れない。
     *
     * @param msg          送信するリクエストオブジェクト
     * @param responseType 待ち受ける応答の message_type
     * @return 応答が届いたときに complete される Future
     */
    public CompletableFuture<JsonNode> sendRequest(Object msg, String responseType) {
        CompletableFuture<JsonNode> future = dispatcher.expectResponse(responseType);
        send(msg);
        return future;
    }
}
