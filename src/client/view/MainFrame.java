package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.presenter.ChatPresenter;
import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.common.GamePhase;
import src.common.Role;
import src.client.state.GameState;
import src.client.state.GameStateListener;

public class MainFrame extends JFrame implements GameStateListener {
    /** 役職発表時のフルスクリーン画像の表示時間(ms)。 */
    private static final int ROLE_OVERLAY_MILLIS = 3000;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    // 役職発表時に役職画像を一定時間だけ画面いっぱいに表示するオーバーレイ
    private final PhaseOverlay roleOverlay = new PhaseOverlay(ROLE_OVERLAY_MILLIS);
    // 直近に発表した役職（同一役職の状態更新で再表示しないため）
    private Role lastShownRole = null;

    public MainFrame(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        super("人狼ゲーム");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);

        cards.add(new LobbyPanel(state, room),                   "LOBBY");
        cards.add(new GamePanel(state, room, noon, night, chat), "GAME");
        add(cards);
        // 役職発表時の全画面画像表示用オーバーレイ
        setGlassPane(roleOverlay);

        state.addListener(this);
        setVisible(true);
    }

    @Override
    public void onStateChanged(GameState state) {
        if (state.phase == GamePhase.LOBBY) {
            cardLayout.show(cards, "LOBBY");
            lastShownRole = null; // 次のゲームでも役職発表演出を再表示できるようにする
        } else {
            cardLayout.show(cards, "GAME");
        }

        // 役職が割り当てられた瞬間に、その役職画像を画面いっぱいに一定時間表示する
        Role role = state.myRole;
        if (role != null && role != lastShownRole) {
            roleOverlay.show(RoleTheme.rawImageFor(role));
            lastShownRole = role;
        }
    }
}
