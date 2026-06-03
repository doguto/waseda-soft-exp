package src.client.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;

final class NightVillageTheme {
    static final Color BACKGROUND_TOP = new Color(7, 12, 27);
    static final Color BACKGROUND_BOTTOM = new Color(1, 4, 13);
    static final Color PANEL_BG = new Color(12, 19, 40);
    static final Color CARD_BG = new Color(16, 25, 51);
    static final Color FIELD_BG = new Color(5, 10, 24);
    static final Color TEXT = new Color(236, 243, 255);
    static final Color MUTED_TEXT = new Color(172, 188, 220);
    static final Color MOON_BLUE = new Color(122, 166, 255);
    static final Color MOON_GLOW = new Color(194, 215, 255);
    static final Color BORDER = new Color(62, 82, 132);
    static final Color BORDER_BRIGHT = new Color(121, 154, 220);
    static final Color DANGER = new Color(111, 34, 45);
    static final Color DANGER_BRIGHT = new Color(190, 75, 88);
    static final Color SUCCESS = new Color(98, 202, 144);
    static final Color GOLD = new Color(221, 166, 54);
    static final Color GOLD_LIGHT = new Color(255, 231, 139);
    static final Color GOLD_DARK = new Color(128, 83, 22);
    static final Color BLOOD = new Color(86, 7, 18);
    static final Color BLOOD_DARK = new Color(33, 4, 11);
    static final Color BLOOD_BRIGHT = new Color(177, 34, 41);

    private NightVillageTheme() {}

    static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_DARK, 1),
                BorderFactory.createLineBorder(BORDER, 1)
            ),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );
    }

    static Border titledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(GOLD_DARK, 1),
            title
        );
        border.setTitleColor(GOLD_LIGHT);
        border.setTitleFont(new Font("Yu Gothic UI", Font.BOLD, 12));
        return BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(6, 8, 8, 8)
        );
    }

    static void styleButton(AbstractButton button) {
        styleButton(button, false);
    }

    static void styleButton(AbstractButton button, boolean danger) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Yu Gothic UI", Font.BOLD, 12));
        button.setBackground(danger ? BLOOD_BRIGHT : BLOOD);
        button.setForeground(GOLD_LIGHT);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(ornateBorder(danger, 6, 14, 6, 14));
    }

    static void styleField(JTextField field) {
        field.setBackground(new Color(3, 7, 17));
        field.setForeground(TEXT);
        field.setCaretColor(MOON_GLOW);
        field.setSelectionColor(new Color(99, 23, 35));
        field.setSelectedTextColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD_DARK, 1),
            BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
    }

    static void styleComboBox(JComboBox<String> box) {
        box.setBackground(BLOOD_DARK);
        box.setForeground(TEXT);
        box.setBorder(BorderFactory.createLineBorder(GOLD_DARK, 1));
        box.setOpaque(true);
    }

    static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createLineBorder(GOLD_DARK, 1));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
    }

    static void keepOwnTheme(JComponent component) {
        component.putClientProperty("noPhaseTheme", Boolean.TRUE);
    }

    static Border ornateBorder(boolean danger, int top, int left, int bottom, int right) {
        Color inner = danger ? BLOOD_BRIGHT : BLOOD;
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(inner, 2),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GOLD_DARK, 1),
                    BorderFactory.createEmptyBorder(top, left, bottom, right)
                )
            )
        );
    }

    static BufferedImage readImage(String fileName) {
        String cp = "/src/client/resources/images/" + fileName;
        try (InputStream in = NightVillageTheme.class.getResourceAsStream(cp)) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }
        File f = new File("src/client/resources/images/" + fileName);
        if (f.exists()) {
            try { return ImageIO.read(f); } catch (Exception ignored) {}
        }
        return null;
    }

    static void drawCoverImage(Graphics2D g2, Image image, int width, int height) {
        if (image == null || width <= 0 || height <= 0) return;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if (imageWidth <= 0 || imageHeight <= 0) return;

        Object oldInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double scale = Math.max((double) width / imageWidth, (double) height / imageHeight);
        int drawWidth = Math.max(1, (int) Math.round(imageWidth * scale));
        int drawHeight = Math.max(1, (int) Math.round(imageHeight * scale));
        int x = (width - drawWidth) / 2;
        int y = (height - drawHeight) / 2;
        g2.drawImage(image, x, y, drawWidth, drawHeight, null);
        if (oldInterpolation != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
    }

    static void paintFallbackGradient(Graphics2D g2, int width, int height) {
        g2.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, 0, height, BACKGROUND_BOTTOM));
        g2.fillRect(0, 0, width, height);
    }

    static void paintOverlay(Graphics2D g2, Color color, float alpha, int width, int height) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.setColor(color);
        g2.fillRect(0, 0, width, height);
        g2.setComposite(old);
    }

    static void applyDarkUiDefaults() {
        UIManager.put("ToolTip.background", new Color(22, 16, 20));
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(GOLD_DARK));
    }
}
