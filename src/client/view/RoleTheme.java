package src.client.view;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import src.common.Role;

/**
 * 役職ごとの画像を管理するユーティリティ。
 *
 * 画像は src/client/resources/images/&lt;role&gt;.png（役職名を小文字にしたファイル名）に
 * 配置する想定。
 * 例: wolf.png, seer.png, knight.png,
 *     villager.png, crazy_villager.png, medium.png
 *
 * 画像が未配置でも null を返すだけで例外にはならないため、
 * 画像を後から追加すれば自動的に表示されるようになる。
 */
public final class RoleTheme {

    /** 役職ラベル横に表示するアイコンの高さ(px)。 */
    private static final int ICON_HEIGHT = 48;

    private static final Map<Role, BufferedImage> IMAGES = new EnumMap<>(Role.class);
    private static final Map<Role, ImageIcon>     ICONS  = new EnumMap<>(Role.class);

    static {
        for (Role role : Role.values()) {
            BufferedImage img = readImage(role.name().toLowerCase() + ".png");
            if (img != null) {
                IMAGES.put(role, img);
                ICONS.put(role, scaleIconByHeight(img, ICON_HEIGHT));
            }
        }
    }

    private RoleTheme() {}

    /** 役職に対応する原寸画像（フルスクリーン表示用、無ければ null）。 */
    public static BufferedImage rawImageFor(Role role) {
        return role == null ? null : IMAGES.get(role);
    }

    /** 役職に対応する小アイコン（役職ラベル横用、無ければ null）。 */
    public static ImageIcon iconFor(Role role) {
        return role == null ? null : ICONS.get(role);
    }

    /** クラスパス → ファイルパスの順で画像を探して読み込む。 */
    private static BufferedImage readImage(String fileName) {
        String cp = "/src/client/resources/images/" + fileName;
        try (InputStream in = RoleTheme.class.getResourceAsStream(cp)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }
        File f = new File("src/client/resources/images/" + fileName);
        if (f.exists()) {
            try { return ImageIO.read(f); } catch (Exception ignored) {}
        }
        return null;
    }

    private static ImageIcon scaleIconByHeight(BufferedImage img, int height) {
        int width = Math.max(1, (int) Math.round(height * (double) img.getWidth() / img.getHeight()));
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
