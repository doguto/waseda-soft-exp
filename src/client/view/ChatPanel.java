package src.client.view;

import src.client.presenter.ChatPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel implements GameStateListener {
    private final ChatPresenter chatPresenter;
    private final JTextArea logArea     = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton    = new JButton("送信");

    public ChatPanel(GameState state, ChatPresenter chatPresenter) {
        this.chatPresenter = chatPresenter;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("チャット"));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);
        add(inputRow, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());
    }

    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        chatPresenter.sendChat(text);
        inputField.setText("");
    }

    @Override
    public void onStateChanged(GameState state) {
        logArea.setText(String.join("\n", state.chatLog));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
