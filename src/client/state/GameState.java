package src.client.state;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import src.common.GamePhase;
import src.common.Role;

public class GameState {
    public String myName = "";
    public Role   myRole = null;
    public String roomId = "";
    public GamePhase phase = GamePhase.LOBBY;
    public List<String> players = new ArrayList<>();
    // 全プレイヤーのうち死亡したプレイヤー名のリスト（表示用）
    public List<String> deadPlayers = new ArrayList<>();
    public List<String> chatLog = new ArrayList<>();
    public List<String> wolfChatLog = new ArrayList<>();
    public List<String> graveChatLog = new ArrayList<>();
    public boolean isAlive = true;
    // 投票で同票が発生したときの情報（クライアント表示用）
    public List<String> lastVoteTieCandidates = new ArrayList<>();
    public int lastVoteTopCount = 0;
    // 議論終了の賛成数表示用
    public int endDiscussionFor = 0;
    public int endDiscussionNeed = 0;
    public int endDiscussionAlive = 0;
<<<<<<< HEAD
=======
    public boolean hasVoted = false;
    public boolean hasNightActionSent = false;
>>>>>>> feature/day-phase-gui

    private final List<GameStateListener> listeners = new ArrayList<>();

    public void addListener(GameStateListener l) {
        listeners.add(l);
    }

    // 受信スレッドから呼ばれる → EDTに切り替えてUI通知
    public void notifyListeners() {
        SwingUtilities.invokeLater(() ->
            listeners.forEach(l -> l.onStateChanged(this))
        );
    }
}
