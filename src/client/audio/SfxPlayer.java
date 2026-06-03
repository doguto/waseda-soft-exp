package src.client.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;

public class SfxPlayer {
    private static final String[] EXTENSIONS = { ".wav", ".aif", ".aiff" };

    public void playWolfAttack() {
        play("wolf_attack");
    }

    public void playWolfAttackFailed() {
        play("wolf_attack_failed");
    }

    public void playScream() {
        play("scream");
    }

    private void play(String baseName) {
        Thread thread = new Thread(() -> playNow(baseName), "sfx-" + baseName);
        thread.setDaemon(true);
        thread.start();
    }

    private void playNow(String baseName) {
        try (AudioInputStream source = openAudio(baseName);
             AudioInputStream decoded = decode(source)) {
            AudioFormat format = decoded.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.open(decoded);
            clip.start();
        } catch (SfxNotFoundException e) {
            System.out.println("[SFX] no sound file found; place " + baseName + ".wav in src/client/resources/sounds/");
        } catch (Exception e) {
            System.err.println("[SFX] error: " + e.getMessage());
        }
    }

    private AudioInputStream openAudio(String baseName) throws Exception {
        for (String extension : EXTENSIONS) {
            String fileName = baseName + extension;
            AudioInputStream stream = openClasspathAudio("/src/client/resources/sounds/" + fileName);
            if (stream != null) return stream;

            stream = openClasspathAudio("/client/resources/sounds/" + fileName);
            if (stream != null) return stream;

            stream = openFileAudio("src/client/resources/sounds/" + fileName);
            if (stream != null) return stream;

            stream = openFileAudio("resources/sounds/" + fileName);
            if (stream != null) return stream;
        }
        throw new SfxNotFoundException();
    }

    private AudioInputStream openClasspathAudio(String path) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) return null;
            return AudioSystem.getAudioInputStream(new BufferedInputStream(stream));
        } catch (Exception ignored) {
            return null;
        }
    }

    private AudioInputStream openFileAudio(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) return null;
            return AudioSystem.getAudioInputStream(file);
        } catch (Exception ignored) {
            return null;
        }
    }

    private AudioInputStream decode(AudioInputStream source) {
        AudioFormat baseFormat = source.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(decodedFormat, source);
    }

    private static class SfxNotFoundException extends Exception {
    }
}
