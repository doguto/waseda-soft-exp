package src.client;

import src.client.network.GameSession;
import src.client.presenter.*;
import src.client.sound.SoundManager;
import src.client.state.GameState;
import src.client.view.MainFrame;

import javax.swing.*;

public class GUIClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameState   state   = new GameState();
            GameSession session = new GameSession();

            RoomPresenter        room  = new RoomPresenter(state, session);
            NoonActionPresenter  noon  = new NoonActionPresenter(state, session);
            NightActionPresenter night = new NightActionPresenter(state, session);
            ChatPresenter        chat  = new ChatPresenter(state, session);

            MessageDispatcher dispatcher = new MessageDispatcher(room, noon, night, chat);
            room.setDispatcher(dispatcher);
            session.setDispatcher(dispatcher);  // sendRequest() 用

            // フェーズ連動BGM（DAY_DISCUSSION / DAY_VOTE で先頭から再生）
            state.addListener(new SoundManager());

            new MainFrame(state, room, noon, night, chat);
        });
    }
}
