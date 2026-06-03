package src.client.view;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    private static final int ROLE_OVERLAY_MILLIS = 3000;
    private static final int WIN_OVERLAY_MILLIS  = 6000;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final PhaseOverlay overlay = new PhaseOverlay(ROLE_OVERLAY_MILLIS);
    private Role lastShownRole = null;
    private boolean winShown = false;
    private boolean executeAnimationShown = false;

    public MainFrame(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        super("人狼ゲーム");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);

        cards.add(new LobbyPanel(state, room),                   "LOBBY");
        cards.add(new GamePanel(state, room, noon, night, chat), "GAME");
        add(cards);
        setGlassPane(overlay);

        state.addListener(this);
        setVisible(true);
    }

    @Override
    public void onStateChanged(GameState state) {
        if (state.phase == GamePhase.LOBBY) {
            cardLayout.show(cards, "LOBBY");
            lastShownRole = null;
            winShown = false;
            executeAnimationShown = false;
        } else {
            cardLayout.show(cards, "GAME");
        }

        // 役職が割り当てられた瞬間に、その役職画像を画面いっぱいに一定時間表示する
        Role role = state.myRole;
        if (role != null && role != lastShownRole) {
            overlay.show(RoleTheme.rawImageFor(role));
            lastShownRole = role;
        }

        // 処刑フェーズ: 演出画像を表示する（タイマーと完了通知は RoomPresenter が管理）
        if (state.phase == GamePhase.EXECUTE && !executeAnimationShown) {
            executeAnimationShown = true;
            BufferedImage execImg = PhaseTheme.rawImageFor(GamePhase.EXECUTE);
            overlay.show(execImg, RoomPresenter.EXECUTE_OVERLAY_MILLIS);
        }
        if (state.phase != GamePhase.EXECUTE) {
            executeAnimationShown = false;
        }

        // ゲーム終了時に、勝利陣営の画像を画面いっぱいに表示する（クリックで閉じればチャットへ戻れる）
        if (state.phase == GamePhase.GAME_OVER && state.winner != null && !winShown) {
            overlay.show(ResultTheme.imageFor(state.winner), WIN_OVERLAY_MILLIS);
            winShown = true;
        }
    }
}
