package src.client.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import src.common.GamePhase;

/**
 * フェーズ（時間帯）ごとの見た目（画像アイコン・背景色）を管理するユーティリティ。
 *
 * <ul>
 *   <li>NIGHT          → status_night.png   （夜）</li>
 *   <li>MORNING        → status_morning.png （朝）</li>
 *   <li>DAY_DISCUSSION / DAY_VOTE → status_day.png （昼）</li>
 *   <li>上記以外（LOBBY/WAITING/EXECUTE/GAME_OVER）→ 画像なし・既定背景色</li>
 * </ul>
 *
 * 背景色は各画像の代表色（平均色）を実行時に算出し、黒文字が読めるよう
 * 白寄りに淡くしたものを使用する（「イメージに基づく」配色）。
 */
public final class PhaseTheme {

    /** 時間帯の区分。 */
    public enum TimeOfDay { MORNING, DAY, NIGHT, NONE }

    /** フェーズ非依存の既定背景色（画像が無い時間帯）。 */
    public static final Color DEFAULT_BACKGROUND = new Color(0xF2, 0xF2, 0xF2);

    /** フェーズ欄に表示するアイコンの幅(px)。高さは元画像の比率で決まる。 */
    private static final int ICON_WIDTH = 150;

    /** 代表色を白に寄せる割合（0.0=そのまま, 1.0=真っ白）。 */
    private static final double LIGHTEN_RATIO = 0.55;

    private static final Map<TimeOfDay, ImageIcon>     ICONS  = new EnumMap<>(TimeOfDay.class);
    private static final Map<TimeOfDay, Color>         COLORS = new EnumMap<>(TimeOfDay.class);
    private static final Map<TimeOfDay, BufferedImage> IMAGES = new EnumMap<>(TimeOfDay.class);

    static {
        load(TimeOfDay.MORNING, "status_morning.png");
        load(TimeOfDay.DAY,     "status_day.png");
        load(TimeOfDay.NIGHT,   "status_night.png");
    }

    private PhaseTheme() {}

    /** フェーズ → 時間帯の対応。 */
    public static TimeOfDay timeOfDay(GamePhase phase) {
        if (phase == null) return TimeOfDay.NONE;
        switch (phase) {
            case NIGHT:          return TimeOfDay.NIGHT;
            case MORNING:        return TimeOfDay.MORNING;
            case DAY_DISCUSSION:
            case DAY_VOTE:       return TimeOfDay.DAY;
            default:             return TimeOfDay.NONE;
        }
    }

    /** フェーズに対応するアイコン（無ければ null）。 */
    public static ImageIcon iconFor(GamePhase phase) {
        return ICONS.get(timeOfDay(phase));
    }

    /** フェーズに対応する背景色（無ければ既定色）。 */
    public static Color backgroundFor(GamePhase phase) {
        return COLORS.getOrDefault(timeOfDay(phase), DEFAULT_BACKGROUND);
    }

    /** フェーズに対応する原寸画像（フルスクリーン表示用、無ければ null）。 */
    public static BufferedImage rawImageFor(GamePhase phase) {
        return IMAGES.get(timeOfDay(phase));
    }

    /**
     * 指定コンポーネント配下のパネル類に背景色を再帰的に適用する。
     * テキスト/リスト/ボタンなどの入力・表示部品は可読性のため変更しない。
     */
    public static void applyBackground(Component comp, Color bg) {
        if (comp instanceof JTextComponent
                || comp instanceof JList
                || comp instanceof AbstractButton
                || comp instanceof JComboBox) {
            return; // 中身の部品はそのまま
        }
        if (comp instanceof JComponent) {
            JComponent jc = (JComponent) comp;
            jc.setOpaque(true);
            jc.setBackground(bg);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyBackground(child, bg);
            }
        }
    }

    // ---- 内部処理 -----------------------------------------------------------

    private static void load(TimeOfDay tod, String fileName) {
        BufferedImage img = readImage(fileName);
        if (img == null) {
            System.err.println("[PhaseTheme] 画像を読み込めませんでした: " + fileName);
            return;
        }
        ICONS.put(tod, scaleIcon(img, ICON_WIDTH));
        COLORS.put(tod, lighten(averageColor(img), LIGHTEN_RATIO));
        IMAGES.put(tod, img);
    }

    /** クラスパス → ファイルパスの順で画像を探して読み込む。 */
    private static BufferedImage readImage(String fileName) {
        String cp = "/src/client/resources/images/" + fileName;
        try (InputStream in = PhaseTheme.class.getResourceAsStream(cp)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }
        // 実行ディレクトリ（プロジェクトルート）からの相対パス
        File f = new File("src/client/resources/images/" + fileName);
        if (f.exists()) {
            try { return ImageIO.read(f); } catch (Exception ignored) {}
        }
        return null;
    }

    private static ImageIcon scaleIcon(BufferedImage img, int width) {
        int height = Math.max(1, (int) Math.round(width * (double) img.getHeight() / img.getWidth()));
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /** 画像の平均色（最大100x100程度にサンプリング）。 */
    private static Color averageColor(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int stepX = Math.max(1, w / 100), stepY = Math.max(1, h / 100);
        long r = 0, g = 0, b = 0, n = 0;
        for (int y = 0; y < h; y += stepY) {
            for (int x = 0; x < w; x += stepX) {
                int rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += rgb & 0xFF;
                n++;
            }
        }
        if (n == 0) return DEFAULT_BACKGROUND;
        return new Color((int) (r / n), (int) (g / n), (int) (b / n));
    }

    /** 色を白方向に ratio だけ寄せる。 */
    private static Color lighten(Color c, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) Math.round(c.getRed()   + (255 - c.getRed())   * ratio);
        int g = (int) Math.round(c.getGreen() + (255 - c.getGreen()) * ratio);
        int b = (int) Math.round(c.getBlue()  + (255 - c.getBlue())  * ratio);
        return new Color(r, g, b);
    }
}
