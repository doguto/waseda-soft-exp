package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.presenter.ChatPresenter;
import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;

public class GamePanel extends JPanel implements GameStateListener {
    private static final Color TOP_COLOR = new Color(8, 14, 32);
    private static final Color BOTTOM_COLOR = new Color(2, 5, 14);
    private static final Color STAR_COLOR = new Color(255, 255, 255, 120);
    private static final Color BORDER_COLOR = new Color(92, 111, 160);

    private final ChatPanel chatPanel;
    private final InformationPanel playerListPanel;
    private final ActionPanel actionPanel;
    private final GameState state;

    public GamePanel(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        this.state = state;
        setLayout(new BorderLayout());
        setOpaque(true);
        // 通常はライトな背景にして、夜フェーズ時だけダークテーマを描画する
        setBackground(new Color(245, 245, 250));

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

        // フェーズ(朝/昼/夜)に応じてウィンドウ全体の背景色を変更
        PhaseTheme.applyBackground(this, PhaseTheme.backgroundFor(state.phase));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 夜フェーズのみ特別な背景を描画
        if (state != null && state.phase == GamePhase.NIGHT) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, TOP_COLOR, 0, getHeight(), BOTTOM_COLOR));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(STAR_COLOR);
                int[][] stars = {
                    {36, 40}, {110, 28}, {210, 68}, {360, 34}, {520, 56},
                    {620, 22}, {760, 50}, {820, 96}, {140, 130}, {295, 118},
                    {448, 140}, {700, 132}
                };
                for (int[] star : stars) {
                    g2.fillOval(star[0], star[1], 2, 2);
                }
            } finally {
                g2.dispose();
            }
        } else {
            super.paintComponent(g);
        }
    }
}
