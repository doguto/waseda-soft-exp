package src.client.ui;

import src.client.state.GamePhase;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.message.SendVillageChatMessage;
import src.message.SendWolfChatMessage;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel implements GameStateListener {
    private final GameState state;
    private final JTextArea logArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("送信");

    public ChatPanel(GameState state) {
        this.state = state;
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
        if (text.isEmpty() || state.connection == null) return;
        try {
            Object msg;
            if ("WOLF".equals(state.myRole) && state.phase == GamePhase.NIGHT) {
                SendWolfChatMessage m = new SendWolfChatMessage();
                m.roomId = state.roomId;
                m.senderName = state.myName;
                m.text = text;
                msg = m;
            } else {
                SendVillageChatMessage m = new SendVillageChatMessage();
                m.roomId = state.roomId;
                m.senderName = state.myName;
                m.text = text;
                msg = m;
            }
            state.connection.send(msg);
            inputField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStateChanged(GameState state) {
        logArea.setText(String.join("\n", state.chatLog));
        // 最新行へスクロール
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
