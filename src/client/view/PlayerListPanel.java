package src.client.view;

import src.client.state.GameState;
import src.client.state.GameStateListener;

import javax.swing.*;
import java.awt.*;

public class PlayerListPanel extends JPanel implements GameStateListener {
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> playerList  = new JList<>(listModel);
    private final JLabel roleLabel  = new JLabel("役職: -");
    private final JLabel phaseLabel = new JLabel("フェーズ: -");

    public PlayerListPanel(GameState state) {
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(160, 0));
        setBorder(BorderFactory.createTitledBorder("プレイヤー"));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(roleLabel);
        infoPanel.add(phaseLabel);
        add(infoPanel, BorderLayout.NORTH);
        add(new JScrollPane(playerList), BorderLayout.CENTER);
    }

    @Override
    public void onStateChanged(GameState state) {
        listModel.clear();
        state.players.forEach(listModel::addElement);
        roleLabel.setText("役職: " + (state.myRole.isEmpty() ? "-" : state.myRole));
        phaseLabel.setText(state.phase.toString());
    }
}
