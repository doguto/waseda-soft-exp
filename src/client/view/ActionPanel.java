package src.client.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import src.client.presenter.NightActionPresenter;
import src.client.presenter.NoonActionPresenter;
import src.client.presenter.RoomPresenter;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.Role;

public class ActionPanel extends JPanel implements GameStateListener {
    private final GameState state;
    private final RoomPresenter roomPresenter;
    private final NoonActionPresenter noonPresenter;
    private final NightActionPresenter nightPresenter;

    public ActionPanel(GameState state, RoomPresenter roomPresenter,
                       NoonActionPresenter noonPresenter, NightActionPresenter nightPresenter) {
        this.state = state;
        this.roomPresenter = roomPresenter;
        this.noonPresenter = noonPresenter;
        this.nightPresenter = nightPresenter;
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        setOpaque(true);
        setBackground(NightVillageTheme.PANEL_BG);
        NightVillageTheme.keepOwnTheme(this);
        setBorder(NightVillageTheme.cardBorder());
        setPreferredSize(new Dimension(0, 92));
    }

    @Override
    public void onStateChanged(GameState state) {
        removeAll();
        if (state.phase == src.common.GamePhase.GAME_OVER) {
            add(statusLabel("ゲーム終了"));
        } else if (!state.isAlive) {
            add(statusLabel("死亡中（墓場チャットのみ可）"));
        } else {
            switch (state.phase) {
                case WAITING -> buildWaitingActions();
                case MORNING -> add(statusLabel("朝（結果発表中）..."));
                case DAY_DISCUSSION -> buildDiscussionActions();
                case DAY_VOTE -> buildVoteActions();
                case EXECUTE -> add(statusLabel("処刑結果表示中..."));
                case NIGHT -> buildNightActions();
                default -> {}
            }
        }
        for (Component component : getComponents()) {
            if (component instanceof JComponent jc) {
                jc.setForeground(NightVillageTheme.TEXT);
            }
        }
        revalidate();
        repaint();
    }

    private void buildWaitingActions() {
        if (state.isHost) {
            javax.swing.JButton startBtn = new javax.swing.JButton("ゲーム開始");
            startBtn.addActionListener(e -> roomPresenter.requestStartGame());
            styleButton(startBtn, false);
            add(startBtn);
        } else {
            add(statusLabel("ホストがゲームを開始するまで待機中"));
        }
    }

    private void buildDiscussionActions() {
        javax.swing.JButton endBtn = new javax.swing.JButton("議論終了");
        endBtn.addActionListener(e -> noonPresenter.requestEndDiscussion());
        styleButton(endBtn, false);
        add(endBtn);

        int forCount = state.endDiscussionFor;
        int alive = state.endDiscussionAlive > 0 ? state.endDiscussionAlive : state.players.size();
        int need = state.endDiscussionNeed;
        int remaining = Math.max(0, need - forCount);
        JLabel countLabel = statusLabel("賛成 " + forCount + " / " + alive + "　あと " + remaining + " 票");
        countLabel.setForeground(remaining == 0 ? NightVillageTheme.SUCCESS : NightVillageTheme.MOON_GLOW);
        add(countLabel);
    }

    private void buildVoteActions() {
        if (state.hasVoted) {
            add(statusLabel("投票済み"));
            return;
        }
        String[] candidates = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .filter(p -> !state.deadPlayers.contains(p))
            .toArray(String[]::new);
        if (candidates.length == 0) {
            add(statusLabel("投票先がありません"));
            return;
        }
        JComboBox<String> box = new JComboBox<>(candidates);
        javax.swing.JButton voteBtn = new javax.swing.JButton("投票");
        voteBtn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) noonPresenter.sendVote(target);
        });
        styleComboBox(box);
        styleButton(voteBtn, true);
        add(statusLabel("投票先:"));
        add(box);
        add(voteBtn);
    }

    private void buildNightActions() {
        if (state.myRole == Role.WOLF) buildWolfActions();
        else if (state.myRole == Role.SEER) buildSeerActions();
        else if (state.myRole == Role.KNIGHT) buildKnightActions();
        else add(statusLabel("夜が明けるまで待機中..."));
    }

    private void buildWolfActions() {
        if (state.hasNightActionSent) {
            add(statusLabel("襲撃済み"));
            return;
        }
        JComboBox<String> box = targetBox();
        javax.swing.JButton btn = new javax.swing.JButton("襲撃");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendWolfAttack(target);
        });
        styleComboBox(box);
        styleButton(btn, true);
        add(statusLabel("襲撃対象:"));
        add(box);
        add(btn);
    }

    private void buildSeerActions() {
        if (state.hasNightActionSent) {
            add(statusLabel("占い済み"));
            return;
        }
        JComboBox<String> box = targetBox();
        javax.swing.JButton btn = new javax.swing.JButton("占い");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendSeerInvestigate(target);
        });
        styleComboBox(box);
        styleButton(btn, false);
        add(statusLabel("占い対象:"));
        add(box);
        add(btn);
    }

    private void buildKnightActions() {
        if (state.hasNightActionSent) {
            add(statusLabel("守護済み"));
            return;
        }
        JComboBox<String> box = targetBox();
        javax.swing.JButton btn = new javax.swing.JButton("守護");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendKnightGuard(target);
        });
        styleComboBox(box);
        styleButton(btn, false);
        add(statusLabel("守護対象:"));
        add(box);
        add(btn);
    }

    private JComboBox<String> targetBox() {
        String[] targets = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .filter(p -> !state.deadPlayers.contains(p))
            .toArray(String[]::new);
        JComboBox<String> box = new JComboBox<>(targets);
        box.setPreferredSize(new Dimension(170, 30));
        box.setPrototypeDisplayValue("長めのプレイヤー名サンプル");
        return box;
    }

    private JLabel statusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Yu Gothic UI", Font.BOLD, 13));
        label.setForeground(NightVillageTheme.TEXT);
        return label;
    }

    private void styleButton(AbstractButton button, boolean danger) {
        NightVillageTheme.styleButton(button, danger);
    }

    private void styleComboBox(JComboBox<String> box) {
        NightVillageTheme.styleComboBox(box);
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? NightVillageTheme.BORDER_BRIGHT : NightVillageTheme.FIELD_BG);
                setForeground(NightVillageTheme.TEXT);
                return this;
            }
        });
    }
}
