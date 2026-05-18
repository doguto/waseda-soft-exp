package src.client.ui;

import src.client.dispatch.MessageDispatcher;
import src.client.network.MessageReceiver;
import src.client.network.ServerConnection;
import src.client.state.GameState;
import src.message.CreateRoomMessage;
import src.message.JoinRoomMessage;

import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {
    private final GameState state;
    private final JTextField hostField  = new JTextField("localhost", 14);
    private final JTextField nameField  = new JTextField("プレイヤー名", 14);
    private final JTextField roomField  = new JTextField("room1", 14);
    private final JLabel statusLabel    = new JLabel(" ");

    public LobbyPanel(GameState state) {
        this.state = state;
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
        c.gridx = 1;              add(joinBtn,   c);

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
            ServerConnection conn = new ServerConnection(host, 8080);
            state.myName = name;
            state.roomId = room;
            state.connection = conn;

            MessageDispatcher dispatcher = new MessageDispatcher(state);
            new MessageReceiver(conn, dispatcher).start();

            Object msg;
            if (isCreate) {
                CreateRoomMessage m = new CreateRoomMessage();
                m.roomId = room; m.name = name;
                msg = m;
            } else {
                JoinRoomMessage m = new JoinRoomMessage();
                m.roomId = room; m.name = name;
                msg = m;
            }
            conn.send(msg);
            statusLabel.setText("接続中...");
        } catch (Exception ex) {
            statusLabel.setText("接続失敗: " + ex.getMessage());
        }
    }
}
