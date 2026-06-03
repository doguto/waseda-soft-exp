package src.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;
import src.common.Role;

public class InformationPanel extends JPanel implements GameStateListener {
    private final GameState gameState;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> playerList = new JList<>(listModel);
    private final JLabel phaseLabel = new JLabel("-");
    private final JLabel phaseImageLabel = new JLabel();
    private final JLabel nameLabel = new JLabel("名前: -");
    private final JLabel roleLabel = new JLabel("役職: -");
    private final JLabel roleImageLabel = new JLabel();

    public InformationPanel(GameState state) {
        this.gameState = state;
        setLayout(new BorderLayout(0, 10));
        setPreferredSize(new Dimension(198, 0));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setOpaque(true);
        setBackground(NightVillageTheme.PANEL_BG);
        NightVillageTheme.keepOwnTheme(this);

        JPanel phasePanel = cardPanel("フェーズ");
        phasePanel.setLayout(new BorderLayout(0, 6));
        phaseLabel.setFont(new Font("Yu Gothic UI", Font.BOLD, 15));
        phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phaseLabel.setForeground(NightVillageTheme.TEXT);
        phaseImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phasePanel.add(phaseImageLabel, BorderLayout.CENTER);
        phasePanel.add(phaseLabel, BorderLayout.SOUTH);

        JPanel infoPanel = cardPanel("自分の情報");
        infoPanel.setLayout(new BorderLayout(0, 8));
        nameLabel.setForeground(NightVillageTheme.TEXT);
        roleLabel.setForeground(NightVillageTheme.MOON_GLOW);
        nameLabel.setFont(new Font("Yu Gothic UI", Font.BOLD, 12));
        roleLabel.setFont(new Font("Yu Gothic UI", Font.BOLD, 13));
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleImageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        roleImageLabel.setToolTipText("クリックで役職説明を表示");
        roleImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRoleDescription();
            }
        });
        JPanel nameRolePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        nameRolePanel.setOpaque(false);
        nameRolePanel.add(nameLabel);
        nameRolePanel.add(roleLabel);
        infoPanel.add(nameRolePanel, BorderLayout.NORTH);
        infoPanel.add(roleImageLabel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(phasePanel, BorderLayout.NORTH);
        northPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel listPanel = cardPanel("プレイヤー");
        listPanel.setLayout(new BorderLayout());
        playerList.setBackground(NightVillageTheme.FIELD_BG);
        playerList.setForeground(NightVillageTheme.TEXT);
        playerList.setSelectionBackground(new Color(49, 74, 137));
        playerList.setSelectionForeground(Color.WHITE);
        playerList.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));

        Font baseFont = new Font("Yu Gothic UI", Font.BOLD, 13);
        @SuppressWarnings("unchecked")
        Font strikeFont = baseFont.deriveFont(Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
        playerList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String nameKey = extractName(value);
            boolean dead = state.deadPlayers.contains(nameKey);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
            if (dead) {
                label.setFont(strikeFont);
                label.setForeground(new Color(150, 160, 184));
                label.setBackground(new Color(18, 21, 33));
            } else {
                label.setFont(baseFont);
                label.setForeground(NightVillageTheme.TEXT);
                label.setBackground(isSelected ? list.getSelectionBackground() : NightVillageTheme.FIELD_BG);
            }
            label.setOpaque(true);
            return label;
        });

        JScrollPane playersScroll = new JScrollPane(playerList);
        NightVillageTheme.styleScrollPane(playersScroll);
        listPanel.add(playersScroll, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
    }

    private JPanel cardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(NightVillageTheme.CARD_BG);
        panel.setBorder(NightVillageTheme.titledBorder(title));
        return panel;
    }

    private void showRoleDescription() {
        Role role = gameState.myRole;
        if (role == null) return;

        JTextArea area = new JTextArea(RoleTheme.descriptionFor(role));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Yu Gothic UI", Font.PLAIN, 13));
        area.setBackground(NightVillageTheme.FIELD_BG);
        area.setForeground(NightVillageTheme.TEXT);
        area.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(area);
        NightVillageTheme.styleScrollPane(sp);
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
        roleImageLabel.setIcon(RoleTheme.infoIconFor(state.myRole));
        listModel.clear();
        boolean gameOver = state.phase == GamePhase.GAME_OVER;
        for (String p : state.players) {
            if (!state.deadPlayers.contains(p)) {
                String entry = gameOver ? playerWithRole(p, state) : p;
                listModel.addElement(entry);
            }
        }
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

    private String extractName(String value) {
        int index = value.indexOf("（");
        return index >= 0 ? value.substring(0, index) : value;
    }
}
