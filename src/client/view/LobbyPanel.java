package src.client.view;

import java.awt.*;
import javax.swing.*;
import src.client.presenter.RoomPresenter;
import src.client.state.GameState;

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

        // 内側のフォームパネルを作成し、BoxLayout で縦にアイテムを揃える
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel title = new JLabel("人狼ゲーム", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        // 入力フィールドを整列させるため、ラベルとフィールドを GridBagLayout に配置
        int fieldWidth = 240;
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 8, 0);

        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.EAST;
        fieldsPanel.add(new JLabel("ホスト:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        hostField.setPreferredSize(new Dimension(fieldWidth, hostField.getPreferredSize().height));
        hostField.setMaximumSize(hostField.getPreferredSize());
        fieldsPanel.add(hostField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.anchor = GridBagConstraints.EAST;
        fieldsPanel.add(new JLabel("プレイヤー名:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        nameField.setPreferredSize(new Dimension(fieldWidth, nameField.getPreferredSize().height));
        nameField.setMaximumSize(nameField.getPreferredSize());
        fieldsPanel.add(nameField, gc);

        gc.gridx = 0; gc.gridy = 2; gc.anchor = GridBagConstraints.EAST;
        fieldsPanel.add(new JLabel("ルームID:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        roomField.setPreferredSize(new Dimension(fieldWidth, roomField.getPreferredSize().height));
        roomField.setMaximumSize(roomField.getPreferredSize());
        fieldsPanel.add(roomField, gc);

        form.add(fieldsPanel);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        // ボタン行
        JButton createBtn = new JButton("ルーム作成");
        JButton joinBtn   = new JButton("ルーム参加");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(createBtn);
        btnRow.add(joinBtn);
        form.add(btnRow);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        // ステータス表示はフォーム下部に中央揃え
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(statusLabel);

        // フォームの表示サイズを固定して、メインパネルに中央配置する
        form.setPreferredSize(new Dimension(420, 260));
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1; c.anchor = GridBagConstraints.CENTER; add(form, c);

        createBtn.addActionListener(e -> connect(true));
        joinBtn.addActionListener(e -> connect(false));
    }

    private void addRow(GridBagConstraints c, int row, String label, JTextField field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.insets = c.insets;
        lc.gridx = 0; lc.gridy = row; lc.anchor = GridBagConstraints.EAST; lc.fill = GridBagConstraints.NONE; lc.weightx = 0;
        add(new JLabel(label), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = c.insets;
        fc.gridx = 1; fc.gridy = row; fc.anchor = GridBagConstraints.WEST; fc.fill = GridBagConstraints.NONE; fc.weightx = 0;
        add(field, fc);
    }

    private void connect(boolean isCreate) {
        String host = hostField.getText().trim();
        String name = nameField.getText().trim();
        String room = roomField.getText().trim();

        if (name.isEmpty() || room.isEmpty()) {
            setStatus("名前とルームIDを入力してください");
            return;
        }

        setStatus("接続中...");
        roomPresenter.connect(host, name, room, isCreate)
            .thenAccept(ok -> {
                // 成功時は特に何もしない（RoomPresenter が状態を更新する）
            })
            .exceptionally(ex -> {
                // CompletableFuture の例外は CompletionException 等でラップされる可能性がある。
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String msg = cause.getMessage() != null ? cause.getMessage() : "接続に失敗しました";
                SwingUtilities.invokeLater(() -> setStatus(msg));
                return null;
            });
    }

    // HTML 解釈による表示崩れを防ぐため、表示文字列をエスケープしてからセットする
    private void setStatus(String text) {
        if (text == null) text = "";
        String escaped = text.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;")
                             .replace("\n", " ");
        statusLabel.setText(escaped);
    }
}
