package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;

public class InformationPanel extends JPanel implements GameStateListener {
    private static final Color PANEL_BG = new Color(11, 18, 38, 220);
    private static final Color BORDER_COLOR = new Color(92, 111, 160);
    private static final Color TEXT_COLOR = new Color(230, 236, 250);
    private static final Color CONTENT_BG = new Color(5, 10, 24);

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> playerList = new JList<>(listModel);
    private final JLabel phaseLabel = new JLabel("-");
    private final JLabel phaseIconLabel = new JLabel();
    private final JLabel nameLabel  = new JLabel("名前: -");
    private final JLabel roleLabel  = new JLabel("役職: -");

    public InformationPanel(GameState state) {
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(160, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setOpaque(false);

        JPanel phasePanel = new JPanel(new BorderLayout());
        phasePanel.setOpaque(false);
        phasePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(Font.BOLD, 13f));
        phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phaseLabel.setForeground(TEXT_COLOR);

        // アイコンとラベルを横並びにするコンテナ
        JPanel phaseContent = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        phaseContent.setOpaque(false);
        phaseIconLabel.setOpaque(false);
        phaseContent.add(phaseIconLabel);
        phaseContent.add(phaseLabel);
        phasePanel.add(phaseContent, BorderLayout.CENTER);
        phaseLabel.setForeground(TEXT_COLOR);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        nameLabel.setForeground(TEXT_COLOR);
        roleLabel.setForeground(TEXT_COLOR);
        infoPanel.add(nameLabel);
        nameLabel.setForeground(TEXT_COLOR);
        roleLabel.setForeground(TEXT_COLOR);
        infoPanel.add(roleLabel);

        JPanel northPanel = new JPanel(new BorderLayout(0, 4));
        northPanel.setOpaque(false);
        northPanel.add(phasePanel, BorderLayout.NORTH);
        northPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        playerList.setBackground(CONTENT_BG);
        playerList.setForeground(TEXT_COLOR);
        playerList.setSelectionBackground(new Color(43, 58, 95));
        playerList.setSelectionForeground(TEXT_COLOR);
        playerList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // カスタムレンダラ: 死亡者は薄い背景で表示、生存者は夜テーマに合わせる
        playerList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            boolean dead = state.deadPlayers.contains(value);
            if (dead) {
                label.setForeground(new Color(180, 180, 200));
                label.setBackground(new Color(30, 30, 40));
                label.setOpaque(true);
            } else {
                label.setForeground(TEXT_COLOR);
                label.setBackground(isSelected ? list.getSelectionBackground() : CONTENT_BG);
                label.setOpaque(true);
            }
            return label;
        });

        JScrollPane playersScroll = new JScrollPane(playerList);
        playersScroll.setOpaque(false);
        playersScroll.getViewport().setOpaque(false);
        listPanel.add(playersScroll, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
    }

    // 月を描画するシンプルなアイコン（右上のフェーズ表示用）
    private static class MoonIcon implements Icon {
        private final int w, h;
        private final Color color;

        MoonIcon(int w, int h, Color color) {
            this.w = w;
            this.h = h;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = Math.min(w, h);
            int cx = x + (w - diameter) / 2;
            int cy = y + (h - diameter) / 2;

            // ベースの丸
            g2.setColor(color);
            g2.fillOval(cx, cy, diameter, diameter);

            // くちばし的に少し右上に背景色で丸を重ねて三日月風に見せる
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setColor(new Color(11, 18, 38));
            int offset = diameter / 4;
            g2.fillOval(cx + offset/2, cy - offset/3, diameter, diameter);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return w; }

        @Override
        public int getIconHeight() { return h; }
    }

    @Override
    public void onStateChanged(GameState state) {
        phaseLabel.setText(state.phase.toString());
        if (state.phase == GamePhase.NIGHT) {
            phaseIconLabel.setIcon(new MoonIcon(16, 16, new Color(255, 235, 120)));
        } else {
            phaseIconLabel.setIcon(null);
        }
        nameLabel.setText("名前: " + (state.myName.isEmpty() ? "-" : state.myName));
        roleLabel.setText("役職: " + (state.myRole == null ? "-" : state.myRole));
        listModel.clear();
        // 生存者を先に表示（players に含まれるが deadPlayers に含まれないもの）
        for (String p : state.players) {
            if (!state.deadPlayers.contains(p)) listModel.addElement(p);
        }
        // その下に死亡者を表示
        for (String d : state.deadPlayers) {
            if (!listModel.contains(d)) listModel.addElement(d);
        }
    }
}
