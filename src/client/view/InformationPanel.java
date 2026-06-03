package src.client.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import src.common.Role;
import src.client.state.GameState;
import src.client.state.GameStateListener;

public class InformationPanel extends JPanel implements GameStateListener {
    private final GameState gameState;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> playerList = new JList<>(listModel);
    private final JLabel phaseLabel = new JLabel("-");
    private final JLabel phaseImageLabel = new JLabel();
    private final JLabel roleLabel  = new JLabel("役職: -");
    private final JLabel roleImageLabel = new JLabel();

    public InformationPanel(GameState state) {
        this.gameState = state;
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(160, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel phasePanel = new JPanel(new BorderLayout(0, 2));
        phasePanel.setBorder(BorderFactory.createTitledBorder("フェーズ"));
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(Font.BOLD, 13f));
        phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 朝/昼/夜に応じた画像を表示する領域
        phaseImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phasePanel.add(phaseImageLabel, BorderLayout.CENTER);
        phasePanel.add(phaseLabel, BorderLayout.SOUTH);

        // 自分の情報: 名前は表示せず、役職名と大きな役職画像を表示する
        JPanel infoPanel = new JPanel(new BorderLayout(0, 4));
        infoPanel.setBorder(BorderFactory.createTitledBorder("自分の情報"));
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 役職画像はクリックで説明を表示できる
        roleImageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        roleImageLabel.setToolTipText("クリックで役職の説明を表示");
        roleImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRoleDescription();
            }
        });
        infoPanel.add(roleLabel, BorderLayout.NORTH);
        infoPanel.add(roleImageLabel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout(0, 4));
        northPanel.add(phasePanel, BorderLayout.NORTH);
        northPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("プレイヤー"));
        // カスタムレンダラで死亡者は灰背景・黒文字で表示
        playerList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            boolean dead = state.deadPlayers.contains(value);
            if (dead) {
                label.setForeground(Color.BLACK);
                label.setBackground(new Color(0xDD, 0xDD, 0xDD));
                label.setOpaque(true);
            } else {
                // 生存者は通常の色。選択時は選択背景を使う。
                label.setForeground(Color.BLACK);
                label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                label.setOpaque(true);
            }
            return label;
        });

        listPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);

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
        phaseLabel.setText(state.phase.toString());
        // 朝/昼/夜に対応する画像を表示（対応が無ければ非表示）
        phaseImageLabel.setIcon(PhaseTheme.iconFor(state.phase));
        roleLabel.setText("役職: " + (state.myRole == null ? "-" : state.myRole));
        // 大きな役職画像（画像が未配置なら null＝非表示）
        roleImageLabel.setIcon(RoleTheme.infoIconFor(state.myRole));
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
