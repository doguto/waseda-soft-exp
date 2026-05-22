package src.client.state;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    public String myName = "";
    public String myRole = "";
    public String roomId = "";
    public GamePhase phase = GamePhase.LOBBY;
    public List<String> players = new ArrayList<>();
    public List<String> chatLog = new ArrayList<>();
    public boolean isAlive = true;

    private final List<GameStateListener> listeners = new ArrayList<>();

    public void addListener(GameStateListener l) {
        listeners.add(l);
    }

    // 受信スレッドから呼ばれる → EDTに切り替えてUI通知
    public void notifyListeners() {
        SwingUtilities.invokeLater(() ->
            listeners.forEach(l -> l.onStateChanged(this))
        );
    }
}
