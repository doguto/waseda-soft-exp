package src.client.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.*;
import src.common.Role;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;

public class InformationPanel extends JPanel implements GameStateListener {
    private static final Color PANEL_BG = new Color(11, 18, 38, 220);
    private static final Color BORDER_COLOR = new Color(92, 111, 160);
    private static final Color TEXT_COLOR = new Color(230, 236, 250);
    private static final Color CONTENT_BG = new Color(5, 10, 24);

    private final GameState gameState;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> playerList = new JList<>(listModel);
    private final JLabel phaseLabel = new JLabel("-");
    private final JLabel phaseImageLabel = new JLabel();
    private final JLabel nameLabel  = new JLabel("名前: -");
    private final JLabel roleLabel  = new JLabel("役職: -");
    private final JLabel roleImageLabel = new JLabel();

    public InformationPanel(GameState state) {
        this.gameState = state;
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(180, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setOpaque(true);
        setBackground(new Color(11, 18, 38));
        putClientProperty("noPhaseTheme", Boolean.TRUE);

        JPanel phasePanel = new JPanel(new BorderLayout(0, 2));
        phasePanel.setOpaque(false);
        phasePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(Font.BOLD, 14f));
        phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phaseLabel.setForeground(TEXT_COLOR);
        phaseImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phaseImageLabel.setOpaque(false);
        phasePanel.add(phaseImageLabel, BorderLayout.CENTER);
        phasePanel.add(phaseLabel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel(new BorderLayout(0, 4));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        nameLabel.setForeground(TEXT_COLOR);
        roleLabel.setForeground(TEXT_COLOR);
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleImageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        roleImageLabel.setToolTipText("クリックで役職の説明を表示");
        roleImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRoleDescription();
            }
        });
        JPanel nameRolePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        nameRolePanel.setOpaque(false);
        nameRolePanel.add(nameLabel);
        nameRolePanel.add(roleLabel);
        infoPanel.add(nameRolePanel, BorderLayout.NORTH);
        infoPanel.add(roleImageLabel, BorderLayout.CENTER);

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

        // カスタムレンダラ: 死亡者は取り消し線＋薄い色で表示、生存者は夜テーマに合わせる
        Font baseFont = playerList.getFont().deriveFont(13f);
        @SuppressWarnings("unchecked")
        Font strikeFont = baseFont.deriveFont(Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
        playerList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            // GAME_OVER 時は "田中（人狼）" 形式の場合、括弧前の名前で判定する
            String nameKey = value.contains("（") ? value.substring(0, value.indexOf("（")) : value;
            boolean dead = state.deadPlayers.contains(nameKey);
            if (dead) {
                label.setFont(strikeFont);
                label.setForeground(new Color(140, 150, 175));
                label.setBackground(new Color(22, 24, 35));
                label.setOpaque(true);
            } else {
                label.setFont(baseFont);
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

    /** 役職画像クリック時に、その役職の説明をダイアログ表示する。 */
    private void showRoleDescription() {
        Role role = gameState.myRole;
        if (role == null) return;

        JTextArea area = new JTextArea(RoleTheme.descriptionFor(role));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        area.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(380, 300));

        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                sp,
                "役職の説明",
                JOptionPane.PLAIN_MESSAGE,
                RoleTheme.iconFor(role));
    }

    @Override
    public void onStateChanged(GameState state) {
        phaseLabel.setText(state.phase.displayName());
        phaseImageLabel.setIcon(PhaseTheme.iconFor(state.phase));
        nameLabel.setText("名前: " + (state.myName.isEmpty() ? "-" : state.myName));
        roleLabel.setText("役職: " + (state.myRole == null ? "-" : state.myRole.displayName()));
        // 大きな役職画像（画像が未配置なら null＝非表示）
        roleImageLabel.setIcon(RoleTheme.infoIconFor(state.myRole));
        listModel.clear();
        boolean gameOver = state.phase == GamePhase.GAME_OVER;
        // 生存者を先に表示（players に含まれるが deadPlayers に含まれないもの）
        for (String p : state.players) {
            if (!state.deadPlayers.contains(p)) {
                String entry = gameOver ? playerWithRole(p, state) : p;
                listModel.addElement(entry);
            }
        }
        // その下に死亡者を表示
        for (String d : state.deadPlayers) {
            if (!listModel.contains(d) && !listModel.contains(playerWithRole(d, state))) {
                String entry = gameOver ? playerWithRole(d, state) : d;
                listModel.addElement(entry);
            }
        }
    }

    private String playerWithRole(String name, GameState state) {
        String role = state.finalRoles.get(name);
        return role != null ? name + "（" + role + "）" : name;
    }
}
