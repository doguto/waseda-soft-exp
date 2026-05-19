package src.client.ui;

import src.client.state.GamePhase;
import src.client.state.GameState;
import src.client.state.GameStateListener;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements GameStateListener {
    private final GameState state = new GameState();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public MainFrame() {
        super("人狼ゲーム");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);

        LobbyPanel lobbyPanel = new LobbyPanel(state);
        GamePanel  gamePanel  = new GamePanel(state);

        cards.add(lobbyPanel, "LOBBY");
        cards.add(gamePanel,  "GAME");
        add(cards);

        state.addListener(this);
        setVisible(true);
    }

    @Override
    public void onStateChanged(GameState state) {
        if (state.phase == GamePhase.LOBBY) {
            cardLayout.show(cards, "LOBBY");
        } else {
            cardLayout.show(cards, "GAME");
        }
    }
}
