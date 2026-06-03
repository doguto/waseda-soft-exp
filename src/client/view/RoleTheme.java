package src.client.view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private static final int ICON_HEIGHT = 56;

    /** 「自分の情報」欄に大きく表示するアイコンの高さ(px)。 */
    private static final int INFO_ICON_HEIGHT = 150;

    private static final Map<Role, BufferedImage> IMAGES     = new EnumMap<>(Role.class);
    private static final Map<Role, ImageIcon>     ICONS      = new EnumMap<>(Role.class);
    private static final Map<Role, ImageIcon>     INFO_ICONS = new EnumMap<>(Role.class);
    private static final Map<Role, String>        DESCRIPTIONS = new EnumMap<>(Role.class);

    static {
        for (Role role : Role.values()) {
            BufferedImage img = readImage(role.name().toLowerCase() + ".png");
            if (img != null) {
                IMAGES.put(role, img);
                ICONS.put(role, scaleIconByHeight(img, ICON_HEIGHT));
                INFO_ICONS.put(role, scaleIconByHeight(img, INFO_ICON_HEIGHT));
            }
            String desc = readDescription(role.name().toLowerCase());
            if (desc != null) {
                DESCRIPTIONS.put(role, desc);
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

    /** 「自分の情報」欄用の大きいアイコン（無ければ null）。 */
    public static ImageIcon infoIconFor(Role role) {
        return role == null ? null : INFO_ICONS.get(role);
    }

    /** 役職の説明文（無ければ既定メッセージ）。 */
    public static String descriptionFor(Role role) {
        if (role == null) return "役職が割り当てられていません。";
        return DESCRIPTIONS.getOrDefault(role, role.name() + " の説明は用意されていません。");
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
        return new ImageIcon(scaleImage(img, width, height));
    }

    private static BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight) {
        int currentWidth = source.getWidth();
        int currentHeight = source.getHeight();
        BufferedImage current = source;

        while (currentWidth / 2 >= targetWidth && currentHeight / 2 >= targetHeight) {
            currentWidth /= 2;
            currentHeight /= 2;
            current = drawScaled(current, currentWidth, currentHeight);
        }
        return drawScaled(current, targetWidth, targetHeight);
    }

    private static BufferedImage drawScaled(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(source, 0, 0, width, height, null);
        } finally {
            g2.dispose();
        }
        return scaled;
    }

    /**
     * 役職説明テキストを読み込む。src/client/resources/roles/&lt;name&gt;.md を優先し、
     * 無ければ .txt を探す。クラスパス → ファイルパスの順で探索する。
     */
    private static String readDescription(String baseName) {
        for (String ext : new String[] { ".md", ".txt" }) {
            String fileName = baseName + ext;
            String cp = "/src/client/resources/roles/" + fileName;
            try (InputStream in = RoleTheme.class.getResourceAsStream(cp)) {
                if (in != null) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
            }
            File f = new File("src/client/resources/roles/" + fileName);
            if (f.exists()) {
                try {
                    return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}
