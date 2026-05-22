package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.state.GameState;
import src.client.state.GameStateListener;

public class InformationPanel extends JPanel implements GameStateListener {
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> playerList = new JList<>(listModel);
    private final JLabel phaseLabel = new JLabel("-");
    private final JLabel nameLabel  = new JLabel("名前: -");
    private final JLabel roleLabel  = new JLabel("役職: -");

    public InformationPanel(GameState state) {
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(160, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel phasePanel = new JPanel(new BorderLayout());
        phasePanel.setBorder(BorderFactory.createTitledBorder("フェーズ"));
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(Font.BOLD, 13f));
        phaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phasePanel.add(phaseLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setBorder(BorderFactory.createTitledBorder("自分の情報"));
        infoPanel.add(nameLabel);
        infoPanel.add(roleLabel);

        JPanel northPanel = new JPanel(new BorderLayout(0, 4));
        northPanel.add(phasePanel, BorderLayout.NORTH);
        northPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("プレイヤー"));
        listPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
    }

    @Override
    public void onStateChanged(GameState state) {
        phaseLabel.setText(state.phase.toString());
        nameLabel.setText("名前: " + (state.myName.isEmpty() ? "-" : state.myName));
        roleLabel.setText("役職: " + (state.myRole.isEmpty() ? "-" : state.myRole));
        listModel.clear();
        state.players.forEach(listModel::addElement);
    }
}
