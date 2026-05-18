package src.client.ui;

import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.message.*;

import javax.swing.*;
import java.awt.*;

public class ActionPanel extends JPanel implements GameStateListener {
    private final GameState state;

    public ActionPanel(GameState state) {
        this.state = state;
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setBorder(BorderFactory.createTitledBorder("アクション"));
        setPreferredSize(new Dimension(0, 80));
    }

    @Override
    public void onStateChanged(GameState state) {
        removeAll();
        switch (state.phase) {
            case WAITING        -> buildWaitingActions();
            case DAY_DISCUSSION -> buildDiscussionActions();
            case DAY_VOTE       -> buildVoteActions();
            case NIGHT          -> buildNightActions();
            case GAME_OVER      -> add(new JLabel("ゲーム終了"));
            default             -> {}
        }
        revalidate();
        repaint();
    }

    private void buildWaitingActions() {
        JButton startBtn = new JButton("ゲーム開始");
        startBtn.addActionListener(e -> sendMsg(() -> {
            StartGameMessage m = new StartGameMessage();
            m.roomId = state.roomId;
            return m;
        }));
        add(startBtn);
        add(new JLabel("（ゲーム開始はホストのみ有効）"));
    }

    private void buildDiscussionActions() {
        JButton endBtn = new JButton("議論終了");
        endBtn.addActionListener(e -> sendMsg(() -> {
            EndDiscussionMessage m = new EndDiscussionMessage();
            m.roomId = state.roomId;
            return m;
        }));
        add(endBtn);
    }

    private void buildVoteActions() {
        String[] candidates = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .toArray(String[]::new);
        JComboBox<String> box = new JComboBox<>(candidates);
        JButton voteBtn = new JButton("投票");
        voteBtn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target == null) return;
            sendMsg(() -> {
                VoteMessage m = new VoteMessage();
                m.roomId = state.roomId;
                m.playerName = state.myName;
                m.targetName = target;
                return m;
            });
        });
        add(new JLabel("投票先:"));
        add(box);
        add(voteBtn);
    }

    private void buildNightActions() {
        switch (state.myRole) {
            case "WOLF"   -> buildWolfActions();
            case "SEER"   -> buildSeerActions();
            case "KNIGHT" -> buildKnightActions();
            default       -> add(new JLabel("夜が明けるまで待機中..."));
        }
    }

    private void buildWolfActions() {
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("襲撃");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target == null) return;
            sendMsg(() -> {
                WolfAttackMessage m = new WolfAttackMessage();
                m.roomId = state.roomId;
                m.wolfName = state.myName;
                m.targetName = target;
                return m;
            });
        });
        add(new JLabel("襲撃対象:")); add(box); add(btn);
    }

    private void buildSeerActions() {
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("占い");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target == null) return;
            sendMsg(() -> {
                SeerInvestigateMessage m = new SeerInvestigateMessage();
                m.roomId = state.roomId;
                m.seerName = state.myName;
                m.targetName = target;
                return m;
            });
        });
        add(new JLabel("占い対象:")); add(box); add(btn);
    }

    private void buildKnightActions() {
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("守護");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target == null) return;
            sendMsg(() -> {
                KnightGuardMessage m = new KnightGuardMessage();
                m.roomId = state.roomId;
                m.knightName = state.myName;
                m.targetName = target;
                return m;
            });
        });
        add(new JLabel("守護対象:")); add(box); add(btn);
    }

    // ── ユーティリティ ────────────────────────────────────────────────────────

    private JComboBox<String> targetBox() {
        String[] targets = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .toArray(String[]::new);
        return new JComboBox<>(targets);
    }

    @FunctionalInterface
    private interface MsgFactory { Object create() throws Exception; }

    private void sendMsg(MsgFactory factory) {
        if (state.connection == null) return;
        try {
            state.connection.send(factory.create());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
