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
        setBorder(BorderFactory.createTitledBorder("アクション"));
        setPreferredSize(new Dimension(0, 80));
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
                case DAY_DISCUSSION -> buildDiscussionActions();
                case DAY_VOTE       -> buildVoteActions();
                case NIGHT          -> buildNightActions();
                default             -> {}
            }
        }
        revalidate();
        repaint();
    }

    private void buildWaitingActions() {
        JButton startBtn = new JButton("ゲーム開始");
        startBtn.addActionListener(e -> roomPresenter.requestStartGame());
        add(startBtn);
        add(new JLabel("（ゲーム開始はホストのみ有効）"));
    }

    private void buildDiscussionActions() {
        JButton endBtn = new JButton("議論終了");
        endBtn.addActionListener(e -> noonPresenter.requestEndDiscussion());
        add(endBtn);

        // 賛成数表示: 表示は "x / 生きている人数（過半数まであと remaining）"
        int forCount = state.endDiscussionFor;
        int alive = state.endDiscussionAlive > 0 ? state.endDiscussionAlive : state.players.size();
        int need = state.endDiscussionNeed;
        int remaining = Math.max(0, need - forCount);
        String label = forCount + " / " + alive + "（過半数まであと " + remaining + "）";
        add(new JLabel(label));
    }

    private void buildVoteActions() {
        String[] candidates = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .toArray(String[]::new);
        JComboBox<String> box = new JComboBox<>(candidates);
        JButton voteBtn = new JButton("投票");
        voteBtn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) noonPresenter.sendVote(target);
        });
        add(new JLabel("投票先:")); add(box); add(voteBtn);
    }

    private void buildNightActions() {
        if (state.myRole == Role.WOLF)        buildWolfActions();
        else if (state.myRole == Role.SEER)   buildSeerActions();
        else if (state.myRole == Role.KNIGHT) buildKnightActions();
        else                                  add(new JLabel("夜が明けるまで待機中..."));
    }

    private void buildWolfActions() {
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("襲撃");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendWolfAttack(target);
        });
        add(new JLabel("襲撃対象:")); add(box); add(btn);
    }

    private void buildSeerActions() {
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("占い");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendSeerInvestigate(target);
        });
        add(new JLabel("占い対象:")); add(box); add(btn);
    }

    private void buildKnightActions() {
        JComboBox<String> box = targetBox();
        JButton btn = new JButton("守護");
        btn.addActionListener(e -> {
            String target = (String) box.getSelectedItem();
            if (target != null) nightPresenter.sendKnightGuard(target);
        });
        add(new JLabel("守護対象:")); add(box); add(btn);
    }

    private JComboBox<String> targetBox() {
        String[] targets = state.players.stream()
            .filter(p -> !p.equals(state.myName))
            .toArray(String[]::new);
        return new JComboBox<>(targets);
    }
}
