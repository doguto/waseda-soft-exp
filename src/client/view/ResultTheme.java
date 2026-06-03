package src.client.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * ゲーム終了時に表示する勝利画像を管理するユーティリティ。
 *
 * 画像は src/client/resources/images/ に配置する想定。
 *   村人陣営の勝利: villager_win.png
 *   人狼陣営の勝利: wolf_win.png
 *
 * 画像が未配置でも null を返すだけで例外にはならない。
 */
public final class ResultTheme {

    private static final BufferedImage WOLF_WIN     = readImage("wolf_win.png");
    private static final BufferedImage VILLAGER_WIN = readImage("villager_win.png");

    private ResultTheme() {}

    /**
     * 勝利陣営に対応する画像を返す（無ければ null）。
     * @param winner "WOLF" または "VILLAGER"
     */
    public static BufferedImage imageFor(String winner) {
        if (winner == null) return null;
        return "WOLF".equals(winner) ? WOLF_WIN : VILLAGER_WIN;
    }

    /** クラスパス → ファイルパスの順で画像を探して読み込む。 */
    private static BufferedImage readImage(String fileName) {
        String cp = "/src/client/resources/images/" + fileName;
        try (InputStream in = ResultTheme.class.getResourceAsStream(cp)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }
        File f = new File("src/client/resources/images/" + fileName);
        if (f.exists()) {
            try { return ImageIO.read(f); } catch (Exception ignored) {}
        }
        return null;
    }
}
