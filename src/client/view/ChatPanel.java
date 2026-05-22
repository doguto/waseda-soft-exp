package src.client.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import src.client.presenter.ChatPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;
import src.common.Role;

public class ChatPanel extends JPanel implements GameStateListener {
    private final ChatPresenter chatPresenter;
    private final JTextArea logArea     = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton    = new JButton("送信");

    private final JToggleButton villageTabBtn = new JToggleButton("全体", true);
    private final JToggleButton wolfTabBtn    = new JToggleButton("人狼");
    private final JToggleButton graveTabBtn   = new JToggleButton("墓地");

    private GameState currentState;

    public ChatPanel(GameState state, ChatPresenter chatPresenter) {
        this.chatPresenter = chatPresenter;
        this.currentState  = state;
        setLayout(new BorderLayout());

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        ButtonGroup tabGroup = new ButtonGroup();
        tabGroup.add(villageTabBtn);
        tabGroup.add(wolfTabBtn);
        tabGroup.add(graveTabBtn);
        villageTabBtn.setFocusPainted(false);
        wolfTabBtn.setFocusPainted(false);
        graveTabBtn.setFocusPainted(false);
        tabPanel.add(villageTabBtn);
        tabPanel.add(wolfTabBtn);
        tabPanel.add(graveTabBtn);
        add(tabPanel, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);
        add(inputRow, BorderLayout.SOUTH);

        villageTabBtn.addActionListener(e -> { refreshLog(currentState); updateSendable(); });
        wolfTabBtn.addActionListener(e -> { refreshLog(currentState); updateSendable(); });
        graveTabBtn.addActionListener(e -> { refreshLog(currentState); updateSendable(); });
        sendButton.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());
    }

    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (wolfTabBtn.isSelected()) {
            chatPresenter.sendWolfChat(text);
        } else if (graveTabBtn.isSelected()) {
            chatPresenter.sendGloveChat(text);
        } else {
            chatPresenter.sendGeneralChat(text);
        }
        inputField.setText("");
    }

    private void refreshLog(GameState state) {
        if (state == null) return;
        List<String> log;
        if (wolfTabBtn.isSelected()) {
            log = state.wolfChatLog;
        } else if (graveTabBtn.isSelected()) {
            log = state.graveChatLog;
        } else {
            log = state.chatLog;
        }
        logArea.setText(String.join("\n", log));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void updateSendable() {
        boolean isWolf = currentState != null && currentState.myRole == Role.WOLF;
        boolean isDead = currentState != null && !currentState.isAlive;

        // タブ自体の有効/無効を更新
        wolfTabBtn.setEnabled(isWolf);
        graveTabBtn.setEnabled(isDead);

        // 無効になったタブが選択中なら全体タブへ切り替え
        if ((wolfTabBtn.isSelected() && !isWolf)
                || (graveTabBtn.isSelected() && !isDead)) {
            villageTabBtn.setSelected(true);
            refreshLog(currentState);
        }

        // 全体タブは DAY_DISCUSSION / WAITING のみ送信可
        boolean canSend;
        if (villageTabBtn.isSelected()) {
            canSend = currentState == null
                    || currentState.phase == GamePhase.DAY_DISCUSSION
                    || currentState.phase == GamePhase.WAITING;
        } else {
            // 人狼・墓地タブはタブが有効な場合のみここに来るので常に送信可
            canSend = true;
        }
        inputField.setEnabled(canSend);
        sendButton.setEnabled(canSend);
    }

    @Override
    public void onStateChanged(GameState state) {
        this.currentState = state;
        refreshLog(state);
        updateSendable();
    }
}
