package src.client.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * 1つの音声ファイル（wav）を再生するプレイヤー。
 *
 * ファイルは src/client/resources/sounds/&lt;fileName&gt; に配置する想定。
 * 読み込みは生成時にバックグラウンドスレッドで行う（大きいBGMでもUIを止めない）。
 * 読み込み完了前に再生を要求された場合は、完了後に自動で再生する。
 */
public class SoundPlayer {
    private final String fileName;
    private Clip clip;
    private boolean wantLoop = false;
    private boolean wantPlayOnce = false;

    public SoundPlayer(String fileName) {
        this.fileName = fileName;
        Thread t = new Thread(this::preload, "sound-preload-" + fileName);
        t.setDaemon(true);
        t.start();
    }

    private void preload() {
        try (AudioInputStream ais = openStream()) {
            Clip c = AudioSystem.getClip();
            c.open(ais);
            synchronized (this) {
                clip = c;
                if (wantLoop) doLoop();
                else if (wantPlayOnce) doPlayOnce();
            }
        } catch (Exception e) {
            System.err.println("[SoundPlayer] 音声を読み込めませんでした: " + fileName + " : " + e);
        }
    }

    /** 先頭から再生し、ループする。 */
    public synchronized void loopFromStart() {
        wantLoop = true;
        wantPlayOnce = false;
        if (clip != null) doLoop();
    }

    /** 先頭から1回だけ再生する。 */
    public synchronized void playOnce() {
        wantPlayOnce = true;
        wantLoop = false;
        if (clip != null) doPlayOnce();
    }

    /** 再生を停止する。 */
    public synchronized void stop() {
        wantLoop = false;
        wantPlayOnce = false;
        if (clip != null) clip.stop();
    }

    private void doLoop() {
        clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private void doPlayOnce() {
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    /** 再生時間をミリ秒で返す。未読み込みなら 0。 */
    public synchronized long getDurationMillis() {
        if (clip == null) return 0L;
        return Math.max(0L, clip.getMicrosecondLength() / 1000L);
    }

    /** クラスパス → ファイルパスの順で音声を開く。 */
    private AudioInputStream openStream() throws Exception {
        String cp = "/src/client/resources/sounds/" + fileName;
        InputStream in = SoundPlayer.class.getResourceAsStream(cp);
        if (in != null) {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
        }
        File f = new File("src/client/resources/sounds/" + fileName);
        return AudioSystem.getAudioInputStream(f);
    }
}
