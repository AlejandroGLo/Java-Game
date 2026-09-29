package attacks;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Procedurally drawn attack icons.
 *
 * The characters themselves are generated in code rather than loaded from
 * image files, so their weapons are too. Keeping every icon here means the
 * inventory menu, the HUD and the in-flight projectile art can all share one
 * definition of what a fireball or a shuriken looks like.
 */
public final class AttackIcons {

    public enum Kind {
        SWORD, SWORD_THROWN, HAMMER, SHIELD,
        FIREBALL, BOLT, ARROW, SHURIKEN,
        HEAL, TELEPORT, BOMB, BUFF
    }

    private AttackIcons() {
    }

    public static void draw(Graphics2D g, Kind kind, int x, int y, int size) {
        Stroke old = g.getStroke();
        switch (kind) {
            case SWORD: sword(g, x, y, size, false); break;
            case SWORD_THROWN: sword(g, x, y, size, true); break;
            case HAMMER: hammer(g, x, y, size); break;
            case SHIELD: shield(g, x, y, size); break;
            case FIREBALL: fireball(g, x, y, size); break;
            case BOLT: bolt(g, x, y, size); break;
            case ARROW: arrow(g, x, y, size); break;
            case SHURIKEN: shuriken(g, x, y, size); break;
            case HEAL: heal(g, x, y, size); break;
            case TELEPORT: teleport(g, x, y, size); break;
            case BOMB: bomb(g, x, y, size); break;
            case BUFF: buff(g, x, y, size); break;
        }
        g.setStroke(old);
    }

    private static void sword(Graphics2D g, int x, int y, int s, boolean spinning) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x + s / 2.0, y + s / 2.0);
        gg.rotate(spinning ? Math.toRadians(35) : Math.toRadians(-35));
        int len = (int) (s * 0.62);
        int bw = Math.max(3, s / 9);

        gg.setColor(new Color(214, 219, 229));
        gg.fillRect(-bw / 2, -len, bw, (int) (len * 1.45));
        gg.setColor(new Color(90, 94, 104));
        gg.drawRect(-bw / 2, -len, bw, (int) (len * 1.45));

        gg.setColor(new Color(176, 140, 60));
        gg.fillRect(-s / 5, (int) (len * 0.42), (int) (s * 0.4), Math.max(2, s / 12));
        gg.setColor(new Color(122, 82, 46));
        gg.fillRect(-bw, (int) (len * 0.5), bw * 2, (int) (s * 0.22));
        gg.dispose();

        if (spinning) {
            g.setColor(new Color(255, 255, 255, 70));
            g.setStroke(new BasicStroke(Math.max(1.5f, s / 22f)));
            g.drawArc(x + s / 8, y + s / 8, s - s / 4, s - s / 4, 20, 250);
        }
    }

    private static void hammer(Graphics2D g, int x, int y, int s) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x + s / 2.0, y + s / 2.0);
        gg.rotate(Math.toRadians(-32));

        gg.setColor(new Color(104, 70, 40));
        gg.fillRect(-Math.max(2, s / 14), -(int) (s * 0.12), Math.max(4, s / 7), (int) (s * 0.6));

        int hw = (int) (s * 0.52), hh = (int) (s * 0.3);
        gg.setColor(new Color(126, 130, 140));
        gg.fillRect(-hw / 2, -(int) (s * 0.4), hw, hh);
        gg.setColor(new Color(72, 76, 86));
        gg.drawRect(-hw / 2, -(int) (s * 0.4), hw, hh);
        gg.setColor(new Color(166, 170, 180));
        gg.fillRect(-hw / 2 + 2, -(int) (s * 0.4) + 2, hw - 4, Math.max(2, hh / 4));
        gg.dispose();
    }

    private static void shield(Graphics2D g, int x, int y, int s) {
        int w = (int) (s * 0.62), h = (int) (s * 0.72);
        int cx = x + s / 2 - w / 2, cy = y + s / 2 - h / 2;
        g.setColor(new Color(120, 180, 235));
        g.fillRoundRect(cx, cy, w, (int) (h * 0.62), 6, 6);
        g.fillPolygon(new int[]{cx, cx + w, cx + w / 2},
                new int[]{cy + (int) (h * 0.58), cy + (int) (h * 0.58), cy + h}, 3);
        g.setColor(new Color(40, 80, 130));
        g.setStroke(new BasicStroke(Math.max(1.5f, s / 22f)));
        g.drawRoundRect(cx, cy, w, (int) (h * 0.62), 6, 6);
        g.setColor(new Color(235, 245, 255));
        g.fillRect(cx + w / 2 - Math.max(1, s / 22), cy + 5, Math.max(2, s / 11), (int) (h * 0.45));
    }

    private static void fireball(Graphics2D g, int x, int y, int s) {
        int cx = x + s / 2, cy = y + s / 2;
        int r = (int) (s * 0.34);
        g.setColor(new Color(255, 120, 30, 90));
        g.fillOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);
        g.setColor(new Color(245, 130, 35));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(new Color(255, 205, 90));
        g.fillOval(cx - r / 2, cy - r / 2 - 1, r, r);
        g.setColor(new Color(255, 245, 210));
        g.fillOval(cx - r / 4, cy - r / 4 - 1, r / 2, r / 2);
        // trailing flames
        g.setColor(new Color(240, 110, 30, 150));
        for (int i = 1; i <= 3; i++) {
            int t = (int) (s * 0.08 * (4 - i));
            g.fillOval(cx - r - i * (s / 8), cy - t / 2, t, t);
        }
    }

    private static void bolt(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(150, 200, 255));
        int[] px = {x + s / 2, x + (int) (s * 0.36), x + (int) (s * 0.52),
                x + (int) (s * 0.36), x + (int) (s * 0.64), x + (int) (s * 0.48)};
        int[] py = {y + (int) (s * 0.14), y + (int) (s * 0.54), y + (int) (s * 0.5),
                y + (int) (s * 0.86), y + (int) (s * 0.46), y + (int) (s * 0.5)};
        g.fillPolygon(px, py, 6);
        g.setColor(new Color(240, 250, 255));
        g.fillPolygon(new int[]{x + s / 2, x + (int) (s * 0.44), x + (int) (s * 0.56)},
                new int[]{y + (int) (s * 0.2), y + (int) (s * 0.52), y + (int) (s * 0.5)}, 3);
    }

    private static void arrow(Graphics2D g, int x, int y, int s) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x + s / 2.0, y + s / 2.0);
        gg.rotate(Math.toRadians(-30));
        int half = (int) (s * 0.38);

        gg.setColor(new Color(150, 105, 60));
        gg.setStroke(new BasicStroke(Math.max(2f, s / 14f)));
        gg.drawLine(-half, 0, half, 0);

        gg.setColor(new Color(215, 220, 230));
        gg.fillPolygon(new int[]{half + (int) (s * 0.16), half - 2, half - 2},
                new int[]{0, -(int) (s * 0.11), (int) (s * 0.11)}, 3);

        gg.setColor(new Color(225, 235, 245));
        for (int i = 0; i < 2; i++) {
            int fx = -half + i * (int) (s * 0.1);
            gg.fillPolygon(new int[]{fx, fx + (int) (s * 0.12), fx + (int) (s * 0.02)},
                    new int[]{0, -(int) (s * 0.13), 0}, 3);
            gg.fillPolygon(new int[]{fx, fx + (int) (s * 0.12), fx + (int) (s * 0.02)},
                    new int[]{0, (int) (s * 0.13), 0}, 3);
        }
        gg.dispose();
    }

    private static void shuriken(Graphics2D g, int x, int y, int s) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x + s / 2.0, y + s / 2.0);
        gg.rotate(Math.toRadians(18));
        int r = (int) (s * 0.4), w = Math.max(2, s / 9);

        gg.setColor(new Color(208, 214, 226));
        for (int i = 0; i < 4; i++) {
            gg.fillPolygon(new int[]{0, -w, w}, new int[]{-r, 0, 0}, 3);
            gg.rotate(Math.PI / 2);
        }
        gg.setColor(new Color(70, 74, 84));
        gg.fillOval(-w / 2, -w / 2, w, w);
        gg.dispose();
    }

    private static void heal(Graphics2D g, int x, int y, int s) {
        int cx = x + s / 2, cy = y + s / 2;
        int arm = (int) (s * 0.4), t = Math.max(3, s / 5);
        g.setColor(new Color(90, 210, 110));
        g.fillRect(cx - t / 2, cy - arm / 2 - t / 4, t, arm);
        g.fillRect(cx - arm / 2, cy - t / 2 - t / 4, arm, t);
        g.setColor(new Color(190, 255, 200, 120));
        g.fillOval(cx - arm, cy - arm, arm * 2, arm * 2);
    }

    private static void teleport(Graphics2D g, int x, int y, int s) {
        int cx = x + s / 2, cy = y + s / 2;
        g.setColor(new Color(170, 120, 240));
        g.setStroke(new BasicStroke(Math.max(1.6f, s / 20f)));
        for (int i = 0; i < 3; i++) {
            int r = (int) (s * (0.18 + i * 0.12));
            g.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
        g.setColor(new Color(235, 220, 255));
        g.fillOval(cx - s / 12, cy - s / 12, s / 6, s / 6);
    }

    private static void bomb(Graphics2D g, int x, int y, int s) {
        int cx = x + s / 2, cy = y + (int) (s * 0.58);
        int r = (int) (s * 0.3);
        g.setColor(new Color(58, 62, 70));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(new Color(110, 116, 128));
        g.fillOval(cx - r / 2, cy - r / 2, r / 2, r / 2);
        g.setColor(new Color(150, 110, 60));
        g.setStroke(new BasicStroke(Math.max(1.5f, s / 20f)));
        g.drawArc(cx, cy - r - (int) (s * 0.14), (int) (s * 0.22), (int) (s * 0.2), 200, 200);
        g.setColor(new Color(255, 190, 70));
        g.fillOval(cx + (int) (s * 0.2), cy - r - (int) (s * 0.16), Math.max(3, s / 9), Math.max(3, s / 9));
    }

    private static void buff(Graphics2D g, int x, int y, int s) {
        int cx = x + s / 2;
        g.setColor(new Color(255, 205, 90));
        g.fillPolygon(new int[]{cx, cx - (int) (s * 0.26), cx + (int) (s * 0.26)},
                new int[]{y + (int) (s * 0.18), y + (int) (s * 0.5), y + (int) (s * 0.5)}, 3);
        g.fillRect(cx - (int) (s * 0.12), y + (int) (s * 0.48), (int) (s * 0.24), (int) (s * 0.3));
        g.setColor(new Color(255, 245, 200, 140));
        g.fillOval(cx - (int) (s * 0.34), y + (int) (s * 0.12), (int) (s * 0.68), (int) (s * 0.68));
    }
}
