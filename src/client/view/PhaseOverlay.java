package src.client.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * フェーズ（朝/昼/夜）が切り替わったときに、対応する画像を
 * ウィンドウいっぱいに一定時間だけ表示するオーバーレイ。
 *
 * JFrame のガラスペインとして使用する。表示中はクリック等を吸収して
 * 背後のゲーム画面に操作が抜けないようにする。
 */
public class PhaseOverlay extends JComponent {
    private final int durationMillis;
    private final Timer hideTimer;
    private BufferedImage image;

    public PhaseOverlay(int durationMillis) {
        this.durationMillis = durationMillis;
        setOpaque(false);
        setVisible(false);
        hideTimer = new Timer(durationMillis, e -> dismiss());
        hideTimer.setRepeats(false);
        // 表示中の操作を吸収し、クリックされたら閉じる（チャット等の操作へ戻れるようにする）
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dismiss();
            }
        });
    }

    /** 既定の表示時間で画面いっぱいに表示する。 */
    public void show(BufferedImage img) {
        show(img, durationMillis);
    }

    /** 指定画像を画面いっぱいに表示し、指定ミリ秒後（またはクリック時）に消す。 */
    public void show(BufferedImage img, int millis) {
        if (img == null) return;
        this.image = img;
        setVisible(true);
        repaint();
        hideTimer.stop();
        hideTimer.setInitialDelay(millis);
        hideTimer.setDelay(millis);
        hideTimer.restart();
    }

    /** オーバーレイを閉じる。 */
    private void dismiss() {
        hideTimer.stop();
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) return;
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 余白対策に背景を黒で塗る
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);
        // アスペクト比を保ったまま、ウィンドウに合わせて拡大はせず収める（contain）。
        // 画像がウィンドウより大きいときだけ縮小し、小さいときは原寸のまま中央に表示する。
        double scale = Math.min(1.0, Math.min((double) w / image.getWidth(),
                                              (double) h / image.getHeight()));
        int dw = (int) Math.round(image.getWidth()  * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        int x = (w - dw) / 2;
        int y = (h - dh) / 2;
        g2.drawImage(image, x, y, dw, dh, null);
        g2.dispose();
    }
}
