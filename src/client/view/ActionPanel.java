package src.client.view;

import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.common.Role;
import src.client.state.GameState;
import src.client.state.GameStateListener;

import javax.swing.*;
import java.awt.*;

public class ActionPanel extends JPanel implements GameStateListener {
    private static final Color PANEL_BG = new Color(11, 18, 38, 220);
    private static final Color BORDER_COLOR = new Color(92, 111, 160);
    private static final Color TEXT_COLOR = new Color(230, 236, 250);
    private static final Color BUTTON_BG = new Color(27, 38, 70);
    private static final Color BUTTON_BORDER = new Color(121, 139, 191);

    private final GameState state;
    private final RoomPresenter roomPresenter;
    private final NoonActionPresenter noonPresenter;
    private final NightActionPresenter nightPresenter;

    public ActionPanel(GameState state, RoomPresenter roomPresenter,
                       NoonActionPresenter noonPresenter, NightActionPresenter nightPresenter) {
        this.state          = state;
        this.roomPresenter  = roomPresenter;
        this.noonPresenter  = noonPresenter;
        this.nightPresenter = nightPresenter;
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setOpaque(true);
        setBackground(new Color(11, 18, 38));
        putClientProperty("noPhaseTheme", Boolean.TRUE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        setPreferredSize(new Dimension(0, 88));
    }

    @Override
    public void onStateChanged(GameState state) {
        removeAll();
        if (state.phase == src.common.GamePhase.GAME_OVER) {
            add(new JLabel("ゲーム終了"));
        } else if (!state.isAlive) {
            add(new JLabel("死亡中（墓場チャットのみ可）"));
        } else {
            switch (state.phase) {
                case WAITING        -> buildWaitingActions();
                case MORNING        -> add(new JLabel("朝（結果発表中）..."));
                case DAY_DISCUSSION -> buildDiscussionActions();
                case DAY_VOTE       -> buildVoteActions();
                case EXECUTE        -> add(new JLabel("処刑結果表示中..."));
                case NIGHT          -> buildNightActions();
                default             -> {}
            }
        }
        for (Component component : getComponents()) {
            if (component instanceof JComponent jc) {
                jc.setForeground(TEXT_COLOR);
            }
        }
        revalidate();
        repaint();
    }

    private void buildWaitingActions() {
        if (state.isHost) {
            JButton startBtn = new JButton("ゲーム開始");
            startBtn.addActionListener(e -> roomPresenter.requestStartGame());
            styleButton(startBtn);
            add(startBtn);
        } else {
            add(new JLabel("ホストがゲームを開始するまでお待ちください"));
        }
    }

    private void buildDiscussionActions() {
        JButton endBtn = new JButton("議論終了");
        endBtn.addActionListener(e -> noonPresenter.requestEndDiscussion());
        styleButton(endBtn);
        add(endBtn);

        // 賛成数表示: 表示は "x / 生きている人数（過半数まであと remaining）"
        int forCount = state.endDiscussionFor;
        int alive = state.endDiscussionAlive > 0 ? state.endDiscussionAlive : state.players.size();
        int need = state.endDiscussionNeed;
        int remaining = Math.max(0, need - forCount);
        JLabel countLabel = new JLabel("賛成 " + forCount + " / " + alive + "　あと " + remaining + " 票");
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 12f));
        countLabel.setForeground(remaining == 0 ? new Color(100, 220, 130) : new Color(200, 210, 240));
        add(countLabel);
    }

    private void buildVoteActions() {
        if (state.hasVoted) {
            add(new JLabel("投票済み"));
            return;
        }
        String[] candidates = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .filter(p -> !state.deadPlayers.contains(p))
            .toArray(String[]::new);
        if (candidates.length == 0) {
            add(new JLabel("投票先がありません"));
            return;
        }
        JComboBox<String> box = new JComboBox<>(candidates);
        JButton voteBtn = new JButton("投票");
        voteBtn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) noonPresenter.sendVote(target);
        });
        styleComboBox(box);
        styleButton(voteBtn);
        add(new JLabel("投票先:")); add(box); add(voteBtn);
    }

    private void buildNightActions() {
        if (state.myRole == Role.WOLF)        buildWolfActions();
        else if (state.myRole == Role.SEER)   buildSeerActions();
        else if (state.myRole == Role.KNIGHT) buildKnightActions();
        else                                  add(new JLabel("夜が明けるまで待機中..."));
    }

    private void buildWolfActions() {
        if (state.hasNightActionSent) {
            add(new JLabel("襲撃済み"));
            return;
        }
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("襲撃");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendWolfAttack(target);
        });
        styleComboBox(box);
        styleButton(btn);
        add(new JLabel("襲撃対象:")); add(box); add(btn);
    }

    private void buildSeerActions() {
        if (state.hasNightActionSent) {
            add(new JLabel("占い済み"));
            return;
        }
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("占い");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendSeerInvestigate(target);
        });
        styleComboBox(box);
        styleButton(btn);
        add(new JLabel("占い対象:")); add(box); add(btn);
    }

    private void buildKnightActions() {
        if (state.hasNightActionSent) {
            add(new JLabel("守護済み"));
            return;
        }
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("守護");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendKnightGuard(target);
        });
        styleComboBox(box);
        styleButton(btn);
        add(new JLabel("守護対象:")); add(box); add(btn);
    }

    private JComboBox<String> targetBox() {
        String[] targets = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .filter(p -> !state.deadPlayers.contains(p))
            .toArray(String[]::new);
        JComboBox<String> box = new JComboBox<>(targets);
        box.setPreferredSize(new Dimension(160, 26));
        box.setOpaque(true);
        box.setBackground(BUTTON_BG);
        box.setForeground(TEXT_COLOR);
        box.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER, 1, true));
        box.setFocusable(true);
        box.setPrototypeDisplayValue("長めのプレイヤー名サンプル");
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? BUTTON_BORDER : BUTTON_BG);
                setForeground(TEXT_COLOR);
                return this;
            }
        });
        return box;
    }

    private void styleButton(AbstractButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER, 1, true));
        button.setOpaque(true);
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setBackground(BUTTON_BG);
        box.setForeground(TEXT_COLOR);
        box.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER, 1, true));
        box.setOpaque(true);
    }
}
