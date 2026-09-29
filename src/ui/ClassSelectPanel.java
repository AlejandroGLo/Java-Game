package ui;

import characters.Character;
import game.Archetype;
import game.CharacterClass;
import game.CharacterPreview;
import game.GameConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Team select: three slots per player, each cycling through the roster with
 * arrow buttons. Portraits breathe on a loop and pop when the pick changes.
 * Start stays disabled until all six slots are filled.
 */
public class ClassSelectPanel extends JPanel {

    private static final CharacterClass[] ROSTER = CharacterClass.values();
    private static final int SLOTS = 6;
    private static final int PER_TEAM = 3;

    private final Consumer<List<Character>> onStart;
    private final Runnable onBack;
    private final Font titleFont;

    private final CharacterClass[] picks = new CharacterClass[SLOTS];
    private final double[] popTime = new double[SLOTS];

    private final Rectangle[] cardRect = new Rectangle[SLOTS];
    private final Rectangle[] leftArrow = new Rectangle[SLOTS];
    private final Rectangle[] rightArrow = new Rectangle[SLOTS];
    private Rectangle startRect = new Rectangle();
    private Rectangle backRect = new Rectangle();

    private int focused = 0;
    private Point mouse = new Point(-1, -1);
    private double clock;
    private long lastNanos;
    private final Timer timer;

    public ClassSelectPanel(Font font, Consumer<List<Character>> onStart, Runnable onBack) {
        this.titleFont = font;
        this.onStart = onStart;
        this.onBack = onBack;

        setBackground(new Color(14, 16, 24));
        setFocusable(true);
        for (int i = 0; i < SLOTS; i++) {
            cardRect[i] = new Rectangle();
            leftArrow[i] = new Rectangle();
            rightArrow[i] = new Rectangle();
        }

        lastNanos = System.nanoTime();
        timer = new Timer(GameConfig.FRAME_DELAY_MS, e -> {
            long now = System.nanoTime();
            double dt = Math.min((now - lastNanos) / 1_000_000_000.0, GameConfig.MAX_FRAME_TIME);
            lastNanos = now;
            clock += dt;
            for (int i = 0; i < SLOTS; i++) {
                if (popTime[i] > 0) popTime[i] = Math.max(0, popTime[i] - dt);
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                handleClick(e.getPoint());
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouse = e.getPoint();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                lastNanos = System.nanoTime();
                timer.start();
                requestFocusInWindow();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                timer.stop();
            }
        });
        timer.start();
    }

    // ---- Interaction ----

    private void handleKey(int code) {
        switch (code) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                cycle(focused, -1);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                cycle(focused, 1);
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                focused = (focused + SLOTS - 1) % SLOTS;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                focused = (focused + 1) % SLOTS;
                break;
            case KeyEvent.VK_TAB:
                focused = (focused + PER_TEAM) % SLOTS;
                break;
            case KeyEvent.VK_ENTER:
                if (isReady()) start();
                break;
            case KeyEvent.VK_ESCAPE:
                onBack.run();
                break;
            default:
                return;
        }
        repaint();
    }

    private void handleClick(Point p) {
        computeLayout();
        for (int i = 0; i < SLOTS; i++) {
            if (leftArrow[i].contains(p)) {
                focused = i;
                cycle(i, -1);
                return;
            }
            if (rightArrow[i].contains(p)) {
                focused = i;
                cycle(i, 1);
                return;
            }
            if (cardRect[i].contains(p)) {
                focused = i;
                repaint();
                return;
            }
        }
        if (startRect.contains(p) && isReady()) start();
        else if (backRect.contains(p)) onBack.run();
    }

    /** Empty slots enter the roster at either end; filled slots wrap around it. */
    private void cycle(int slot, int dir) {
        if (picks[slot] == null) {
            picks[slot] = dir > 0 ? ROSTER[0] : ROSTER[ROSTER.length - 1];
        } else {
            int idx = (picks[slot].ordinal() + dir + ROSTER.length) % ROSTER.length;
            picks[slot] = ROSTER[idx];
        }
        popTime[slot] = GameConfig.SELECT_POP_SECONDS;
        repaint();
    }

    private boolean isReady() {
        for (CharacterClass c : picks) {
            if (c == null) return false;
        }
        return true;
    }

    private void start() {
        List<Character> roster = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            int playerId = i < PER_TEAM ? 1 : 2;
            int indexInTeam = (i % PER_TEAM) + 1;
            // Numbered so duplicate picks stay distinguishable in the battle HUD.
            roster.add(picks[i].create(picks[i].getLabel() + " " + indexInTeam, playerId));
        }
        onStart.accept(roster);
    }

    // ---- Layout ----

    private void computeLayout() {
        int w = Math.max(getWidth(), 900);
        int h = Math.max(getHeight(), 600);
        int cx = w / 2;

        int gridTop = 165;
        int gridBottom = h - 120;
        int rowH = Math.max(140, Math.min(200, (gridBottom - gridTop) / PER_TEAM));
        int cardW = 240;
        int cardH = rowH - 14;

        int leftColX = cx - 300 - cardW / 2;
        int rightColX = cx + 300 - cardW / 2;

        for (int i = 0; i < SLOTS; i++) {
            int col = i < PER_TEAM ? leftColX : rightColX;
            int row = i % PER_TEAM;
            int y = gridTop + row * rowH;
            cardRect[i].setBounds(col, y, cardW, cardH);

            int arrowY = y + cardH - 34;
            leftArrow[i].setBounds(col + 10, arrowY, 34, 28);
            rightArrow[i].setBounds(col + cardW - 44, arrowY, 34, 28);
        }

        startRect.setBounds(cx - 130, h - 92, 260, 54);
        backRect.setBounds(40, h - 92, 160, 54);
    }

    // ---- Rendering ----

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        computeLayout();

        int w = getWidth(), h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(18, 20, 32), 0, h, new Color(8, 9, 14)));
        g.fillRect(0, 0, w, h);

        g.setFont(titleFont.deriveFont(44f));
        g.setColor(Color.WHITE);
        drawCentered(g, "CHOOSE YOUR CHAMPIONS", w / 2, 72);

        g.setFont(titleFont.deriveFont(26f));
        g.setColor(new Color(120, 180, 255));
        drawCentered(g, "PLAYER 1", cardRect[0].x + cardRect[0].width / 2, 132);
        g.setColor(new Color(255, 140, 140));
        drawCentered(g, "PLAYER 2", cardRect[3].x + cardRect[3].width / 2, 132);

        for (int i = 0; i < SLOTS; i++) {
            drawSlot(g, i);
        }

        drawButton(g, startRect, "START", isReady());
        drawButton(g, backRect, "BACK", true);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(150, 155, 170));
        drawCentered(g, "←/→ change character    ↑/↓ change slot    ENTER start    ESC back",
                w / 2, h - 18);
    }

    private void drawSlot(Graphics2D g, int i) {
        Rectangle r = cardRect[i];
        boolean isFocused = i == focused;
        boolean p1 = i < PER_TEAM;

        g.setColor(new Color(26, 29, 42));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setStroke(new BasicStroke(isFocused ? 3f : 1.5f));
        g.setColor(isFocused
                ? (p1 ? new Color(120, 180, 255) : new Color(255, 140, 140))
                : new Color(58, 62, 80));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setStroke(new BasicStroke(1f));

        CharacterClass pick = picks[i];
        int spriteH = r.height - 78;
        int spriteW = (int) (spriteH * (15.0 / 23.0));
        int spriteX = r.x + r.width / 2 - spriteW / 2;
        int spriteY = r.y + 12;

        if (pick == null) {
            g.setColor(new Color(70, 74, 92));
            g.setFont(new Font("SansSerif", Font.BOLD, 46));
            drawCentered(g, "?", r.x + r.width / 2, spriteY + spriteH / 2 + 16);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(new Color(120, 126, 145));
            drawCentered(g, "EMPTY", r.x + r.width / 2, r.y + r.height - 48);
        } else {
            double breath = clock + i * GameConfig.SELECT_BREATH_SLOT_OFFSET;
            double pop = popScale(i);

            // Contact shadow, so the portrait reads as standing rather than floating.
            int shadowW = (int) (spriteW * 1.05 * pop);
            g.setColor(new Color(0, 0, 0, 90));
            g.fillOval(r.x + r.width / 2 - shadowW / 2, spriteY + spriteH - 5, shadowW, 9);

            CharacterPreview.draw(g, pick, spriteX, spriteY, spriteW, spriteH, breath, pop);

            g.setFont(new Font("SansSerif", Font.BOLD, 19));
            g.setColor(Color.WHITE);
            drawCentered(g, pick.getLabel(), r.x + r.width / 2, r.y + r.height - 50);

            Archetype a = pick.getArchetype();
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(archetypeColor(a));
            drawCentered(g, a.getLabel(), r.x + r.width / 2, r.y + r.height - 32);
        }

        drawArrow(g, leftArrow[i], false);
        drawArrow(g, rightArrow[i], true);
    }

    /** Scales from SELECT_POP_START_SCALE up to 1, overshooting slightly on the way. */
    private double popScale(int i) {
        if (popTime[i] <= 0) return 1;
        double p = 1 - popTime[i] / GameConfig.SELECT_POP_SECONDS;
        double c1 = GameConfig.SELECT_POP_OVERSHOOT;
        double c3 = c1 + 1;
        double q = p - 1;
        double eased = 1 + c3 * q * q * q + c1 * q * q;
        double start = GameConfig.SELECT_POP_START_SCALE;
        return start + (1 - start) * eased;
    }

    private static Color archetypeColor(Archetype a) {
        switch (a) {
            case LIGHT: return new Color(120, 220, 230);
            case TANK: return new Color(245, 170, 90);
            default: return new Color(215, 218, 230);
        }
    }

    private void drawArrow(Graphics2D g, Rectangle r, boolean pointRight) {
        boolean hover = r.contains(mouse);
        g.setColor(hover ? new Color(58, 64, 88) : new Color(38, 42, 58));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);

        g.setColor(hover ? Color.YELLOW : new Color(200, 205, 220));
        int midY = r.y + r.height / 2;
        int x1 = pointRight ? r.x + 12 : r.x + r.width - 12;
        int x2 = pointRight ? r.x + r.width - 12 : r.x + 12;
        g.fillPolygon(new int[]{x1, x1, x2}, new int[]{midY - 7, midY + 7, midY}, 3);
    }

    private void drawButton(Graphics2D g, Rectangle r, String text, boolean enabled) {
        boolean hover = enabled && r.contains(mouse);
        g.setColor(enabled ? (hover ? new Color(62, 78, 52) : new Color(38, 46, 34)) : new Color(30, 32, 40));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g.setColor(enabled ? (hover ? Color.YELLOW : new Color(190, 220, 170)) : new Color(80, 84, 98));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g.setStroke(new BasicStroke(1f));

        g.setFont(titleFont.deriveFont(26f));
        drawCentered(g, text, r.x + r.width / 2, r.y + r.height / 2 + 9);
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y) {
        g.drawString(text, cx - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
