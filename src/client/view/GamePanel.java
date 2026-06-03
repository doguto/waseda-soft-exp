package src.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import src.client.presenter.ChatPresenter;
import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;

public class GamePanel extends JPanel implements GameStateListener {
    private static final Color STAR_COLOR = new Color(255, 255, 255, 125);
    private static final Color MOON_COLOR = new Color(226, 235, 255, 235);
    private static final Color MOON_SHADOW = new Color(7, 12, 27, 210);
    private static final Color VILLAGE_SHADOW = new Color(0, 2, 8, 190);
    private static final BufferedImage DAY_BACKGROUND =
        NightVillageTheme.readImage("day_village_background.png");
    private static final BufferedImage NIGHT_BACKGROUND =
        NightVillageTheme.readImage("night_village_background.png");

    private final ChatPanel chatPanel;
    private final InformationPanel playerListPanel;
    private final ActionPanel actionPanel;
    private final GameState state;

    public GamePanel(GameState state, RoomPresenter room, NoonActionPresenter noon,
                     NightActionPresenter night, ChatPresenter chat) {
        this.state = state;
        setLayout(new BorderLayout(10, 10));
        setOpaque(true);
        setBackground(NightVillageTheme.BACKGROUND_BOTTOM);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        NightVillageTheme.applyDarkUiDefaults();

        chatPanel = new ChatPanel(state, chat);
        playerListPanel = new InformationPanel(state);
        actionPanel = new ActionPanel(state, room, noon, night);

        add(chatPanel, BorderLayout.CENTER);
        add(playerListPanel, BorderLayout.EAST);
        add(actionPanel, BorderLayout.SOUTH);

        state.addListener(this);
    }

    @Override
    public void onStateChanged(GameState state) {
        chatPanel.onStateChanged(state);
        playerListPanel.onStateChanged(state);
        actionPanel.onStateChanged(state);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean night = state != null && state.phase == GamePhase.NIGHT;
            BufferedImage background = night ? NIGHT_BACKGROUND : DAY_BACKGROUND;
            if (background != null) {
                NightVillageTheme.drawCoverImage(g2, background, getWidth(), getHeight());
                NightVillageTheme.paintOverlay(
                    g2,
                    night ? new Color(0, 0, 0) : new Color(5, 10, 22),
                    night ? 0.38f : 0.24f,
                    getWidth(),
                    getHeight()
                );
            } else {
                NightVillageTheme.paintFallbackGradient(g2, getWidth(), getHeight());
                paintStars(g2);
                if (night) {
                    paintNightMood(g2);
                }
            }
            paintEdgeShade(g2);
        } finally {
            g2.dispose();
        }
    }

    private void paintStars(Graphics2D g2) {
        int[][] stars = {
            {32, 36}, {94, 72}, {176, 28}, {260, 88}, {338, 48}, {470, 78},
            {586, 34}, {680, 92}, {760, 52}, {836, 116}, {420, 150}, {144, 138}
        };
        g2.setColor(STAR_COLOR);
        for (int i = 0; i < stars.length; i++) {
            int size = i % 3 == 0 ? 3 : 2;
            g2.fillOval(stars[i][0], stars[i][1], size, size);
        }
    }

    private void paintNightMood(Graphics2D g2) {
        int moonX = Math.max(30, getWidth() - 170);
        int moonY = 34;
        g2.setColor(new Color(80, 120, 220, 36));
        g2.fillOval(moonX - 30, moonY - 24, 132, 132);
        g2.setColor(MOON_COLOR);
        g2.fillOval(moonX, moonY, 72, 72);
        g2.setColor(MOON_SHADOW);
        g2.fillOval(moonX + 20, moonY - 8, 70, 82);

        g2.setColor(new Color(160, 188, 255, 120));
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.drawString("Moonlit Village", 24, 36);

        int baseY = getHeight() - 42;
        g2.setColor(VILLAGE_SHADOW);
        g2.fillPolygon(new int[] {24, 56, 88}, new int[] {baseY, baseY - 26, baseY}, 3);
        g2.fillRect(36, baseY, 40, 28);
        g2.fillPolygon(new int[] {96, 130, 164}, new int[] {baseY, baseY - 34, baseY}, 3);
        g2.fillRect(108, baseY, 44, 32);
        g2.fillRect(0, baseY + 22, getWidth(), 48);

        g2.setColor(new Color(202, 65, 78, 130));
        g2.fillOval(210, baseY - 8, 5, 3);
        g2.fillOval(224, baseY - 8, 5, 3);
    }

    private void paintEdgeShade(Graphics2D g2) {
        g2.setPaint(new GradientPaint(
            0, 0, new Color(0, 0, 0, 125),
            0, Math.max(1, getHeight() / 2), new Color(0, 0, 0, 0)
        ));
        g2.fillRect(0, 0, getWidth(), Math.max(1, getHeight() / 2));
        g2.setPaint(new GradientPaint(
            0, Math.max(1, getHeight() / 2), new Color(0, 0, 0, 0),
            0, getHeight(), new Color(0, 0, 0, 165)
        ));
        g2.fillRect(0, Math.max(1, getHeight() / 2), getWidth(), Math.max(1, getHeight() / 2));
    }
}
