package src.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import src.client.presenter.RoomPresenter;
import src.client.state.GameState;

public class LobbyPanel extends JPanel {
    private final RoomPresenter roomPresenter;
    private final JTextField hostField = new JTextField("localhost", 14);
    private final JTextField nameField = new JTextField("プレイヤー名", 14);
    private final JTextField roomField = new JTextField("room1", 14);
    private final JLabel statusLabel = new JLabel(" ");
    private final Image backgroundImage;
    private final ImageIcon buttonIcon;

    public LobbyPanel(GameState state, RoomPresenter roomPresenter) {
        this.roomPresenter = roomPresenter;
        this.backgroundImage = loadImage("lobbybackground.png");
        this.buttonIcon = loadIcon("lobbybutton.png");
        setLayout(new GridBagLayout());
        setBackground(new Color(37, 32, 48));
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel title = new JLabel("人狼ゲーム", SwingConstants.CENTER);
        title.setFont(new Font("MS Mincho", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(Box.createVerticalStrut(20));

        int fieldWidth = 240;
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 8, 0);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.EAST;
        JLabel hostLabel = createFieldLabel("ホスト: ");
        fieldsPanel.add(hostLabel, gc);
        gc.gridx = 1;
        gc.anchor = GridBagConstraints.WEST;
        setFieldWidth(hostField, fieldWidth);
        fieldsPanel.add(hostField, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.EAST;
        JLabel nameLabel = createFieldLabel("プレイヤー名: ");
        fieldsPanel.add(nameLabel, gc);
        gc.gridx = 1;
        gc.anchor = GridBagConstraints.WEST;
        setFieldWidth(nameField, fieldWidth);
        fieldsPanel.add(nameField, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.anchor = GridBagConstraints.EAST;
        JLabel roomIdLabel = createFieldLabel("ルームID: ");
        fieldsPanel.add(roomIdLabel, gc);
        gc.gridx = 1;
        gc.anchor = GridBagConstraints.WEST;
        setFieldWidth(roomField, fieldWidth);
        fieldsPanel.add(roomField, gc);

        form.add(fieldsPanel);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton createBtn = new JButton("ルーム作成", buttonIcon);
        JButton joinBtn = new JButton("ルーム参加", buttonIcon);
        styleLobbyButton(createBtn);
        styleLobbyButton(joinBtn);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(createBtn);
        btnRow.add(joinBtn);
        form.add(btnRow);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(statusLabel);

        form.setPreferredSize(new Dimension(420, 260));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        add(form, c);

        createBtn.addActionListener(e -> connect(true));
        joinBtn.addActionListener(e -> connect(false));
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    private void setFieldWidth(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, field.getPreferredSize().height));
        field.setMaximumSize(field.getPreferredSize());
    }

    private void styleLobbyButton(JButton button) {
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setForeground(Color.WHITE);
        if (buttonIcon != null) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
        }
    }

    private void connect(boolean isCreate) {
        String host = hostField.getText().trim();
        String name = nameField.getText().trim();
        String room = roomField.getText().trim();

        if (name.isEmpty() || room.isEmpty()) {
            setStatus("名前とルームIDを入力してください", Color.RED);
            return;
        }

        setStatus("接続中...", Color.WHITE);
        roomPresenter.connect(host, name, room, isCreate)
            .exceptionally(ex -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String msg = cause.getMessage() != null ? cause.getMessage() : "接続に失敗しました";
                SwingUtilities.invokeLater(() -> setStatus(msg, Color.RED));
                return null;
            });
    }

    private void setStatus(String text, Color color) {
        if (text == null) text = "";
        String escaped = text.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;")
                             .replace("\n", " ");
        statusLabel.setText(escaped);
        statusLabel.setForeground(color);
    }

    private static ImageIcon loadIcon(String fileName) {
        BufferedImage image = readImage(fileName);
        return image == null ? null : new ImageIcon(image);
    }

    private static Image loadImage(String fileName) {
        return readImage(fileName);
    }

    private static BufferedImage readImage(String fileName) {
        String classpath = "/src/client/resources/images/" + fileName;
        try (InputStream in = LobbyPanel.class.getResourceAsStream(classpath)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }

        File file = new File("src/client/resources/images/" + fileName);
        if (file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
