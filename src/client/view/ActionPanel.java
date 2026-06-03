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
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
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
        for (Component component : getComponents()) {
            if (component instanceof JComponent jc) {
                jc.setForeground(TEXT_COLOR);
            }
        }
        revalidate();
        repaint();
    }

    private void buildWaitingActions() {
        JButton startBtn = new JButton("ゲーム開始");
        startBtn.addActionListener(e -> roomPresenter.requestStartGame());
        styleButton(startBtn);
        add(startBtn);
        add(new JLabel("（ゲーム開始はホストのみ有効）"));
    }

    private void buildDiscussionActions() {
        JButton endBtn = new JButton("議論終了");
        endBtn.addActionListener(e -> noonPresenter.requestEndDiscussion());
        styleButton(endBtn);
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
