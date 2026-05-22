package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.presenter.ChatPresenter;
import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;

public class GamePanel extends JPanel implements GameStateListener {
    private final ChatPanel chatPanel;
    private final InformationPanel playerListPanel;
    private final ActionPanel actionPanel;

    public GamePanel(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        setLayout(new BorderLayout());

        chatPanel       = new ChatPanel(state, chat);
        playerListPanel = new InformationPanel(state);
        actionPanel     = new ActionPanel(state, room, noon, night);

        add(chatPanel,       BorderLayout.CENTER);
        add(playerListPanel, BorderLayout.EAST);
        add(actionPanel,     BorderLayout.SOUTH);

        state.addListener(this);
    }

    @Override
    public void onStateChanged(GameState state) {
        chatPanel.onStateChanged(state);
        playerListPanel.onStateChanged(state);
        actionPanel.onStateChanged(state);
    }
}
