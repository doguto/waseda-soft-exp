package src.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import src.client.presenter.ChatPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;
import src.common.Role;

public class ChatPanel extends JPanel implements GameStateListener {
    private static final Color DISABLED_TEXT = new Color(116, 111, 100);
    private static final Color OTHER_BUBBLE = new Color(237, 213, 151, 236);
    private static final Color OTHER_TEXT = new Color(54, 37, 24);
    private static final Color OWN_BUBBLE = new Color(125, 19, 30, 238);
    private static final Color OWN_TEXT = new Color(255, 238, 177);
    private static final Color SYSTEM_BUBBLE = new Color(7, 10, 20, 206);
    private static final BufferedImage DAY_BACKGROUND =
        NightVillageTheme.readImage("day_village_background.png");
    private static final BufferedImage NIGHT_BACKGROUND =
        NightVillageTheme.readImage("night_village_background.png");

    private final ChatPresenter chatPresenter;
    private final MessageListPanel messageList = new MessageListPanel();
    private final JScrollPane scrollPane = new JScrollPane(messageList);
    private final JTextField inputField = new JTextField(14);
    private final JButton sendButton = new JButton("送信");

    private final JToggleButton villageTabBtn = new JToggleButton("全体", true);
    private final JToggleButton wolfTabBtn = new JToggleButton("人狼");
    private final JToggleButton graveTabBtn = new JToggleButton("墓地");

    private GameState currentState;

    public ChatPanel(GameState state, ChatPresenter chatPresenter) {
        this.chatPresenter = chatPresenter;
        this.currentState = state;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBackground(NightVillageTheme.PANEL_BG);
        setBorder(NightVillageTheme.cardBorder());
        NightVillageTheme.keepOwnTheme(this);

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tabPanel.setOpaque(false);
        ButtonGroup tabGroup = new ButtonGroup();
        tabGroup.add(villageTabBtn);
        tabGroup.add(wolfTabBtn);
        tabGroup.add(graveTabBtn);
        styleTab(villageTabBtn);
        styleTab(wolfTabBtn);
        styleTab(graveTabBtn);
        tabPanel.add(villageTabBtn);
        tabPanel.add(wolfTabBtn);
        tabPanel.add(graveTabBtn);
        add(tabPanel, BorderLayout.NORTH);

        messageList.setOpaque(false);
        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                messageList.revalidate();
                messageList.repaint();
            }
        });
        NightVillageTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        NightVillageTheme.styleField(inputField);
        NightVillageTheme.styleButton(sendButton);
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);
        add(inputRow, BorderLayout.SOUTH);

        villageTabBtn.addActionListener(e -> { refreshLog(currentState); updateSendable(); });
        wolfTabBtn.addActionListener(e -> { refreshLog(currentState); updateSendable(); });
        graveTabBtn.addActionListener(e -> { refreshLog(currentState); updateSendable(); });
        sendButton.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());
        updateTabColors();
        refreshLog(state);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            boolean night = isNightPhase();
            BufferedImage background = night ? NIGHT_BACKGROUND : DAY_BACKGROUND;
            if (background != null) {
                NightVillageTheme.drawCoverImage(g2, background, getWidth(), getHeight());
                NightVillageTheme.paintOverlay(
                    g2,
                    night ? Color.BLACK : new Color(6, 12, 22),
                    night ? 0.48f : 0.34f,
                    getWidth(),
                    getHeight()
                );
            } else {
                NightVillageTheme.paintFallbackGradient(g2, getWidth(), getHeight());
            }
        } finally {
            g2.dispose();
        }
    }

    private void styleTab(JToggleButton button) {
        button.setUI(new BasicToggleButtonUI());
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFont(new Font("Yu Gothic UI", Font.BOLD, 13));
        button.setBorder(NightVillageTheme.ornateBorder(false, 7, 20, 7, 20));
        button.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                updateTabColors();
            }
        });
    }

    private void updateTabColors() {
        for (JToggleButton tab : new JToggleButton[] { villageTabBtn, wolfTabBtn, graveTabBtn }) {
            if (!tab.isEnabled()) {
                tab.setBackground(new Color(16, 14, 15));
                tab.setForeground(DISABLED_TEXT);
            } else if (tab.isSelected()) {
                tab.setBackground(NightVillageTheme.BLOOD_BRIGHT);
                tab.setForeground(NightVillageTheme.GOLD_LIGHT);
            } else {
                tab.setBackground(NightVillageTheme.BLOOD_DARK);
                tab.setForeground(NightVillageTheme.MUTED_TEXT);
            }
        }
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
        messageList.removeAll();
        if (state == null) {
            messageList.revalidate();
            messageList.repaint();
            return;
        }

        List<String> log;
        if (wolfTabBtn.isSelected()) {
            log = state.wolfChatLog;
        } else if (graveTabBtn.isSelected()) {
            log = state.graveChatLog;
        } else {
            log = state.chatLog;
        }

        for (String line : log) {
            addBubbleRow(line, isOwnMessage(line), isSystemMessage(line));
        }
        messageList.add(Box.createVerticalGlue());
        messageList.revalidate();
        messageList.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void addBubbleRow(String line, boolean own, boolean system) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        ChatBubble bubble = new ChatBubble(line, own, system);
        if (system) {
            JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            center.setOpaque(false);
            center.add(bubble);
            row.add(center, BorderLayout.CENTER);
        } else {
            row.add(bubble, own ? BorderLayout.EAST : BorderLayout.WEST);
        }
        messageList.add(row);
    }

    private boolean isOwnMessage(String line) {
        if (currentState == null || currentState.myName == null || currentState.myName.isBlank() || line == null) {
            return false;
        }
        String sender = extractSender(line);
        if (sender != null) {
            return sender.equals(currentState.myName);
        }
        return line.contains(currentState.myName + ":") || line.contains(currentState.myName + "：");
    }

    private boolean isSystemMessage(String line) {
        if (line == null) return false;
        String sender = extractSender(line);
        if (sender != null) {
            return sender.equalsIgnoreCase("SYSTEM")
                || sender.equalsIgnoreCase("System")
                || sender.contains("システム");
        }
        return line.startsWith("[") || line.startsWith("【");
    }

    private String extractSender(String line) {
        if (line == null) return null;
        int start = Math.max(line.lastIndexOf('】'), line.lastIndexOf(']'));
        int colon = line.indexOf(':', start + 1);
        if (colon < 0) colon = line.indexOf('：', start + 1);
        if (colon < 0) return null;
        String sender = line.substring(Math.max(0, start + 1), colon).trim();
        return sender.isEmpty() ? null : sender;
    }

    private void updateSendable() {
        boolean isWolf = currentState != null && currentState.myRole == Role.WOLF;
        boolean isDead = currentState != null && !currentState.isAlive;

        wolfTabBtn.setEnabled(isWolf);
        graveTabBtn.setEnabled(isDead);

        if ((wolfTabBtn.isSelected() && !isWolf)
                || (graveTabBtn.isSelected() && !isDead)) {
            villageTabBtn.setSelected(true);
            refreshLog(currentState);
        }

        boolean canSend;
        if (villageTabBtn.isSelected()) {
            canSend = currentState != null && (
                    currentState.isAlive
                    && (currentState.phase == GamePhase.DAY_DISCUSSION || currentState.phase == GamePhase.WAITING)
                    || currentState.phase == GamePhase.GAME_OVER
            );
        } else {
            canSend = true;
        }
        inputField.setEnabled(canSend);
        sendButton.setEnabled(canSend);
        updateTabColors();
    }

    private boolean isNightPhase() {
        return currentState != null && currentState.phase == GamePhase.NIGHT;
    }

    @Override
    public void onStateChanged(GameState state) {
        this.currentState = state;
        if (state != null && !state.isAlive) {
            graveTabBtn.setSelected(true);
        }
        refreshLog(state);
        updateSendable();
        repaint();
    }

    private final class MessageListPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isNightPhase() ? new Color(0, 0, 0, 178) : new Color(2, 6, 16, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private final class ChatBubble extends JPanel {
        private static final int ARC = 18;
        private static final int PAD_X = 14;
        private static final int PAD_Y = 9;
        private static final int TAIL = 10;
        private static final int GAP = 5;

        private final String text;
        private final boolean own;
        private final boolean system;

        ChatBubble(String text, boolean own, boolean system) {
            this.text = text == null ? "" : text;
            this.own = own;
            this.system = system;
            setOpaque(false);
            setFont(new Font("Yu Gothic UI", Font.BOLD, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int maxTextWidth = maxTextWidth();
            List<String> lines = wrapLines(fm, maxTextWidth);
            int width = 0;
            for (String line : lines) {
                width = Math.max(width, fm.stringWidth(line));
            }
            width += PAD_X * 2 + (system ? 0 : TAIL);
            int height = Math.max(1, lines.size()) * fm.getHeight() + PAD_Y * 2;
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2.getFontMetrics(getFont());
                List<String> lines = wrapLines(fm, maxTextWidth());
                Color bubbleColor = system ? SYSTEM_BUBBLE : (own ? OWN_BUBBLE : OTHER_BUBBLE);
                Color borderColor = system ? NightVillageTheme.GOLD_DARK : NightVillageTheme.GOLD;
                Color textColor = system ? NightVillageTheme.GOLD_LIGHT : (own ? OWN_TEXT : OTHER_TEXT);

                int bubbleX = own || system ? 0 : TAIL;
                int bubbleW = getWidth() - (system ? 0 : TAIL);
                int bubbleH = getHeight();
                g2.setColor(bubbleColor);
                g2.fillRoundRect(bubbleX, 0, bubbleW - 1, bubbleH - 1, ARC, ARC);
                if (!system) {
                    Polygon tail = new Polygon();
                    int mid = Math.min(bubbleH - 12, 22);
                    if (own) {
                        tail.addPoint(getWidth() - TAIL, mid - 5);
                        tail.addPoint(getWidth() - 1, mid);
                        tail.addPoint(getWidth() - TAIL, mid + 7);
                    } else {
                        tail.addPoint(TAIL, mid - 5);
                        tail.addPoint(0, mid);
                        tail.addPoint(TAIL, mid + 7);
                    }
                    g2.fillPolygon(tail);
                }
                g2.setColor(borderColor);
                g2.drawRoundRect(bubbleX, 0, bubbleW - 1, bubbleH - 1, ARC, ARC);

                g2.setFont(getFont());
                g2.setColor(textColor);
                int x = bubbleX + PAD_X;
                int y = PAD_Y + fm.getAscent();
                for (String line : lines) {
                    g2.drawString(line, x, y);
                    y += fm.getHeight();
                }
            } finally {
                g2.dispose();
            }
        }

        private int maxTextWidth() {
            int viewportWidth = scrollPane.getViewport().getWidth();
            int baseWidth = viewportWidth > 0 ? viewportWidth : 720;
            double ratio = system ? 0.72 : 0.58;
            return Math.max(180, Math.min(560, (int) (baseWidth * ratio)));
        }

        private List<String> wrapLines(FontMetrics fm, int maxWidth) {
            List<String> lines = new ArrayList<>();
            for (String paragraph : text.split("\\R", -1)) {
                if (paragraph.isEmpty()) {
                    lines.add("");
                    continue;
                }
                StringBuilder current = new StringBuilder();
                for (int i = 0; i < paragraph.length(); i++) {
                    char ch = paragraph.charAt(i);
                    String candidate = current.toString() + ch;
                    if (current.length() > 0 && fm.stringWidth(candidate) > maxWidth) {
                        lines.add(current.toString());
                        current.setLength(0);
                    }
                    current.append(ch);
                }
                if (current.length() > 0) {
                    lines.add(current.toString());
                }
            }
            if (lines.isEmpty()) {
                lines.add("");
            }
            return lines;
        }
    }
}
