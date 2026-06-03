package src.client.audio;

import javax.sound.sampled.*;
import java.io.*;

public class BgmPlayer {
    private Clip clip;

    // 再生をループで開始。ファイルはプロジェクト内のいくつかの候補を探す。
    public void playLoop() {
        if (clip != null && clip.isRunning()) return;

        AudioInputStream ais = null;
        try {
            // 候補パス（開発中は src 配下、配布時は resources 配下を想定）
            String[] paths = new String[] {
                "src/client/resources/sounds/night_bgm.wav",
                "resources/sounds/night_bgm.wav",
                "/client/resources/sounds/night_bgm.wav"
            };

            for (String p : paths) {
                try {
                    if (p.startsWith("/")) {
                        InputStream is = getClass().getResourceAsStream(p);
                        if (is != null) {
                            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                            break;
                        }
                    } else {
                        File f = new File(p);
                        if (f.exists()) {
                            ais = AudioSystem.getAudioInputStream(f);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // 該当候補で失敗したら次へ
                }
            }

            if (ais == null) {
                System.out.println("[BGM] no bgm file found; place night_bgm.wav in src/client/resources/sounds/");
                return;
            }

            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, ais);

            DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(din);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            System.out.println("[BGM] playing loop");

        } catch (Exception e) {
            System.err.println("[BGM] error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ais != null) {
                try { ais.close(); } catch (IOException ignored) {}
            }
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.flush();
            clip.close();
            clip = null;
            System.out.println("[BGM] stopped");
        }
    }
}
