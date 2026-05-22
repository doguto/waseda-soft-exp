package src.client.view;

import src.client.presenter.RoomPresenter;
import src.client.state.GameState;

import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {
    private final RoomPresenter roomPresenter;
    private final JTextField hostField  = new JTextField("localhost", 14);
    private final JTextField nameField  = new JTextField("プレイヤー名", 14);
    private final JTextField roomField  = new JTextField("room1", 14);
    private final JLabel statusLabel    = new JLabel(" ");

    public LobbyPanel(GameState state, RoomPresenter roomPresenter) {
        this.roomPresenter = roomPresenter;
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("人狼ゲーム", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(title, c);

        c.gridwidth = 1;
        addRow(c, 1, "ホスト:",         hostField);
        addRow(c, 2, "プレイヤー名:",   nameField);
        addRow(c, 3, "ルームID:",       roomField);

        JButton createBtn = new JButton("ルーム作成");
        JButton joinBtn   = new JButton("ルーム参加");
        c.gridx = 0; c.gridy = 4; add(createBtn, c);
        c.gridx = 1;              add(joinBtn, c);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        add(statusLabel, c);

        createBtn.addActionListener(e -> connect(true));
        joinBtn.addActionListener(e -> connect(false));
    }

    private void addRow(GridBagConstraints c, int row, String label, JTextField field) {
        c.gridx = 0; c.gridy = row; add(new JLabel(label), c);
        c.gridx = 1;               add(field, c);
    }

    private void connect(boolean isCreate) {
        String host = hostField.getText().trim();
        String name = nameField.getText().trim();
        String room = roomField.getText().trim();

        if (name.isEmpty() || room.isEmpty()) {
            statusLabel.setText("名前とルームIDを入力してください");
            return;
        }

        try {
            roomPresenter.connect(host, name, room, isCreate);
            statusLabel.setText("接続中...");
        } catch (Exception ex) {
            statusLabel.setText("接続失敗: " + ex.getMessage());
        }
    }
}
