package src.client.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
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
    private static final int ROLE_INTRO_MILLIS = 1500;
    private static final int ROLE_OVERLAY_MILLIS = 3000;
    /** 勝利画像の表示時間(ms)。クリックで早めに閉じることもできる。 */
    private static final int WIN_OVERLAY_MILLIS = 6000;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    // 役職発表・勝利演出に使う、画面いっぱいの画像オーバーレイ（一定時間/クリックで消える）
    private final PhaseOverlay overlay = new PhaseOverlay(ROLE_OVERLAY_MILLIS);
    private final BufferedImage roleIntroImage = readImage("role_intro_you_are.png");
    private Timer pendingRoleOverlayTimer;
    // 直近に発表した役職（同一役職の状態更新で再表示しないため）
    private Role lastShownRole = null;
    // 勝利画像を表示済みか（GAME_OVER 中の状態更新で再表示しないため）
    private boolean winShown = false;

    public MainFrame(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        super("人狼ゲーム");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);

        cards.add(new LobbyPanel(state, room),                   "LOBBY");
        cards.add(new GamePanel(state, room, noon, night, chat), "GAME");
        add(cards);
        // 役職発表・勝利演出用の全画面画像オーバーレイ
        setGlassPane(overlay);

        state.addListener(this);
        setVisible(true);
    }

    @Override
    public void onStateChanged(GameState state) {
        if (state.phase == GamePhase.LOBBY) {
            cardLayout.show(cards, "LOBBY");
            cancelPendingRoleOverlay();
            lastShownRole = null; // 次のゲームでも役職発表演出を再表示できるようにする
            winShown = false;
        } else {
            cardLayout.show(cards, "GAME");
        }

        // 役職が割り当てられた瞬間に、その役職画像を画面いっぱいに一定時間表示する
        Role role = state.myRole;
        if (role != null && role != lastShownRole) {
            showRoleOverlay(role);
            lastShownRole = role;
        }

        // ゲーム終了時に、勝利陣営の画像を画面いっぱいに表示する（クリックで閉じればチャットへ戻れる）
        if (state.phase == GamePhase.GAME_OVER && state.winner != null && !winShown) {
            overlay.show(ResultTheme.imageFor(state.winner), WIN_OVERLAY_MILLIS);
            winShown = true;
        }
    }

    private void showRoleOverlay(Role role) {
        BufferedImage roleImage = RoleTheme.rawImageFor(role);
        if (roleIntroImage == null || roleImage == null) {
            overlay.show(roleImage);
            return;
        }

        cancelPendingRoleOverlay();
        overlay.show(roleIntroImage, ROLE_INTRO_MILLIS);
        pendingRoleOverlayTimer = new Timer(ROLE_INTRO_MILLIS, e -> {
            overlay.show(roleImage, ROLE_OVERLAY_MILLIS);
            cancelPendingRoleOverlay();
        });
        pendingRoleOverlayTimer.setRepeats(false);
        pendingRoleOverlayTimer.start();
    }

    private void cancelPendingRoleOverlay() {
        if (pendingRoleOverlayTimer != null) {
            pendingRoleOverlayTimer.stop();
            pendingRoleOverlayTimer = null;
        }
    }

    private static BufferedImage readImage(String fileName) {
        String classpath = "/src/client/resources/images/" + fileName;
        try (InputStream in = MainFrame.class.getResourceAsStream(classpath)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }

        File file = new File("src/client/resources/images/" + fileName);
        if (file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
