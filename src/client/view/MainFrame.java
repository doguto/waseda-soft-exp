package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.presenter.ChatPresenter;
import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.common.GamePhase;
import src.client.state.GameState;
import src.client.state.GameStateListener;

public class MainFrame extends JFrame implements GameStateListener {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public MainFrame(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        super("人狼ゲーム");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);

        cards.add(new LobbyPanel(state, room),                   "LOBBY");
        cards.add(new GamePanel(state, room, noon, night, chat), "GAME");
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
