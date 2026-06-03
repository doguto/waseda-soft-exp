package src.client.sound;

import javax.swing.Timer;
import src.client.state.GameState;
import src.client.state.GameStateListener;
import src.common.GamePhase;

/**
 * フェーズに応じてBGMを制御するマネージャ。
 *
 * 現状の挙動:
 *   - 議論フェーズ(DAY_DISCUSSION) / 投票フェーズ(DAY_VOTE) に入ったら bell.wav を鳴らしてから
 *     discussion_and_vote.wav をループ再生
 *   - 夜フェーズ(NIGHT)に入ったら night.wav を先頭から再生
 *   - それ以外のフェーズに変わったら停止
 *
 * フェーズが変化したときだけ処理する（チャット等の更新では再再生しない）。
 */
public class SoundManager implements GameStateListener {
    private static final int NIGHT_BGM_DELAY_MILLIS = 5000;

    private final SoundPlayer bell = new SoundPlayer("bell.wav");
    private final SoundPlayer discussionVoteBgm = new SoundPlayer("discussion_and_vote.wav");
    private final SoundPlayer nightBgm = new SoundPlayer("night.wav");
    private final SoundPlayer wolfWinBgm = new SoundPlayer("wolf_win_bgm.wav");
    private final SoundPlayer villagerWinBgm = new SoundPlayer("villager_win_bgm.wav");
    private GamePhase lastPhase = null;
    private Timer pendingDayBgmTimer;
    private Timer pendingNightBgmTimer;

    @Override
    public void onStateChanged(GameState state) {
        GamePhase phase = state.phase;
        if (phase == lastPhase) return; // フェーズ変化時のみ反応する
        lastPhase = phase;

        Track track = switch (phase) {
            case DAY_DISCUSSION, DAY_VOTE -> Track.DAY;
            case NIGHT -> Track.NIGHT;
            case GAME_OVER -> Track.RESULT;
            default -> Track.NONE;
        };

        switch (track) {
            case DAY -> startDayBgmWithBell();
            case NIGHT -> {
                cancelPendingDayBgm();
                cancelPendingNightBgm();
                discussionVoteBgm.stop();
                nightBgm.stop();
                stopWinBgm();
                startNightBgmAfterDelay();
            }
            case RESULT -> startWinBgm(state.winner);
            default -> {
                cancelPendingDayBgm();
                cancelPendingNightBgm();
                discussionVoteBgm.stop();
                nightBgm.stop();
                stopWinBgm();
            }
        }
    }

    private void startDayBgmWithBell() {
        cancelPendingDayBgm();
        cancelPendingNightBgm();
        nightBgm.stop();
        discussionVoteBgm.stop();
        stopWinBgm();
        bell.playOnce();

        long delayMillis = Math.max(1L, bell.getDurationMillis());
        pendingDayBgmTimer = new Timer((int) Math.min(Integer.MAX_VALUE, delayMillis), e -> {
            discussionVoteBgm.loopFromStart();
            cancelPendingDayBgm();
        });
        pendingDayBgmTimer.setRepeats(false);
        pendingDayBgmTimer.start();
    }

    private void startNightBgmAfterDelay() {
        pendingNightBgmTimer = new Timer(NIGHT_BGM_DELAY_MILLIS, e -> {
            nightBgm.loopFromStart();
            cancelPendingNightBgm();
        });
        pendingNightBgmTimer.setRepeats(false);
        pendingNightBgmTimer.start();
    }

    private void startWinBgm(String winner) {
        cancelPendingDayBgm();
        cancelPendingNightBgm();
        discussionVoteBgm.stop();
        nightBgm.stop();
        stopWinBgm();

        if ("WOLF".equals(winner)) {
            wolfWinBgm.playOnce();
        } else if ("VILLAGER".equals(winner)) {
            villagerWinBgm.playOnce();
        }
    }

    private void stopWinBgm() {
        wolfWinBgm.stop();
        villagerWinBgm.stop();
    }

    private void cancelPendingDayBgm() {
        if (pendingDayBgmTimer != null) {
            pendingDayBgmTimer.stop();
            pendingDayBgmTimer = null;
        }
    }

    private void cancelPendingNightBgm() {
        if (pendingNightBgmTimer != null) {
            pendingNightBgmTimer.stop();
            pendingNightBgmTimer = null;
        }
    }

    private enum Track {
        DAY,
        NIGHT,
        RESULT,
        NONE
    }
}
