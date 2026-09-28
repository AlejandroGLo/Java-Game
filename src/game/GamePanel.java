package game;

import characters.Character;
import moves.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class GamePanel extends JPanel implements ActionListener, KeyListener, BattleContext {

    private final List<Character> allCharacters;
    private final List<Character> turnOrder;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Fx> effects = new ArrayList<>();
    private final List<Dust> dust = new ArrayList<>();
    private final Consumer<String> onGameOver;
    private final Runnable onQuit;
    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Timer timer;
    private final Set<Integer> pressed = new HashSet<>();
    private final Map<String, CharacterSprite> sprites = new HashMap<>();
    private final Map<Character, Animator> animators = new HashMap<>();

    private double worldWidth = 1200;
    private double groundY = 620;
    private boolean initialized = false;
    private boolean gameOver = false;
    private boolean turnLocked = false;
    private boolean charging = false;
    private boolean paused = false;
    private int turnIndex = 0;
    private double turnTimeLeft;
    private double postActionTime;
    private double aimAngle = 30;
    private double power = 0;
    private String lastLog = "";
    private double logTime = 0;

    private long lastNanos;
    private double accumulator;

    private static class Dust {
        double x, y, vx, vy, life;
    }

    /** A short-lived burst: an explosion, or the smaller flash at a weapon muzzle. */
    private static class Fx {
        double x, y, life, maxLife, size;

        Fx(double x, double y, double life, double size) {
            this.x = x;
            this.y = y;
            this.life = life;
            this.maxLife = life;
            this.size = size;
        }
    }

    public GamePanel(List<Character> roster, Consumer<String> onGameOver, Runnable onQuit) {
        this.allCharacters = new ArrayList<>(roster);
        this.onGameOver = onGameOver;
        this.onQuit = onQuit;
        this.turnOrder = buildTurnOrder(this.allCharacters);
        this.turnTimeLeft = GameConfig.TURN_SECONDS;

        sprites.put("Knight", SpriteFactory.knight());
        sprites.put("Wizard", SpriteFactory.wizard());
        sprites.put("Archer", SpriteFactory.archer());
        sprites.put("Ninja", SpriteFactory.ninja());
        for (Character c : allCharacters) {
            animators.put(c, new Animator());
        }

        setPreferredSize(new Dimension(1200, 700));
        setFocusable(true);
        addKeyListener(this);

        lastNanos = System.nanoTime();
        timer = new Timer(GameConfig.FRAME_DELAY_MS, this);
        timer.start();
    }

    private List<Character> buildTurnOrder(List<Character> roster) {
        List<Character> p1 = new ArrayList<>();
        List<Character> p2 = new ArrayList<>();
        for (Character c : roster) {
            if (c.getPlayerId() == 1) p1.add(c);
            else p2.add(c);
        }
        List<Character> order = new ArrayList<>();
        int max = Math.max(p1.size(), p2.size());
        for (int i = 0; i < max; i++) {
            if (i < p1.size()) order.add(p1.get(i));
            if (i < p2.size()) order.add(p2.get(i));
        }
        return order;
    }

    public void requestFocusOnBattle() {
        requestFocusInWindow();
    }

    private Character getActive() {
        if (turnOrder.isEmpty()) return null;
        return turnOrder.get(turnIndex);
    }

    private Animator anim(Character c) {
        return animators.computeIfAbsent(c, k -> new Animator());
    }

    // ---- Game loop ----

    /**
     * Real elapsed time is fed into a fixed-size physics step, so the simulation
     * behaves identically regardless of how often the timer actually fires and
     * fast projectiles can't skip through thin platforms.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double frame = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;
        if (frame > GameConfig.MAX_FRAME_TIME) frame = GameConfig.MAX_FRAME_TIME;

        accumulator += frame;
        while (accumulator >= GameConfig.PHYSICS_STEP) {
            update(GameConfig.PHYSICS_STEP);
            accumulator -= GameConfig.PHYSICS_STEP;
        }
        repaint();
    }

    private void update(double dt) {
        int w = getWidth() > 0 ? getWidth() : 1200;
        int h = getHeight() > 0 ? getHeight() : 700;
        worldWidth = w;
        groundY = h - GameConfig.GROUND_MARGIN;

        if (!initialized) {
            initPositions();
            initialized = true;
        }
        if (gameOver || paused) return;

        if (!turnLocked) {
            handleActiveInput(dt);
        }

        updateCharacterPhysics(dt);
        updateAnimators(dt);
        updateProjectiles(dt);
        updateEffects(dt);

        if (turnLocked && projectiles.isEmpty()) {
            postActionTime -= dt;
            if (postActionTime <= 0) nextTurn();
        }
        if (logTime > 0) logTime -= dt;
    }

    private void buildObstacles() {
        obstacles.clear();
        int gy = (int) groundY;
        int w = (int) worldWidth;
        int plat = 20;
        // low blocks (step stones), mirrored
        obstacles.add(new Rectangle((int) (w * 0.27), gy - 55, 70, 55));
        obstacles.add(new Rectangle((int) (w * 0.73) - 70, gy - 55, 70, 55));
        // floating platforms (upper level)
        obstacles.add(new Rectangle((int) (w * 0.33), gy - 150, 130, plat));
        obstacles.add(new Rectangle((int) (w * 0.67) - 130, gy - 150, 130, plat));
        // central pillar (mid level)
        obstacles.add(new Rectangle(w / 2 - 25, gy - 110, 50, 110));
    }

    private Rectangle obstacleAt(double x, double y) {
        for (Rectangle r : obstacles) {
            if (x < r.x + r.width && x + Character.WIDTH > r.x
                    && y < r.y + r.height && y + Character.HEIGHT > r.y) {
                return r;
            }
        }
        return null;
    }

    private void initPositions() {
        buildObstacles();
        int i1 = 0, i2 = 0;
        for (Character c : allCharacters) {
            if (c.getPlayerId() == 1) {
                double fx = worldWidth * (0.12 + 0.10 * i1);
                c.setPosition(fx, groundY - Character.HEIGHT);
                c.setFacingRight(true);
                i1++;
            } else {
                double fx = worldWidth * (0.88 - 0.10 * i2) - Character.WIDTH;
                c.setPosition(fx, groundY - Character.HEIGHT);
                c.setFacingRight(false);
                i2++;
            }
        }
    }

    private static double approach(double value, double target, double maxDelta) {
        if (value < target) return Math.min(value + maxDelta, target);
        return Math.max(value - maxDelta, target);
    }

    private void handleActiveInput(double dt) {
        Character active = getActive();
        if (active == null || !active.isAlive()) return;

        double dx = 0;
        if (pressed.contains(KeyEvent.VK_LEFT) || pressed.contains(KeyEvent.VK_A)) {
            dx = -1;
            active.setFacingRight(false);
        }
        if (pressed.contains(KeyEvent.VK_RIGHT) || pressed.contains(KeyEvent.VK_D)) {
            dx = 1;
            active.setFacingRight(true);
        }

        // Ease into and out of full speed instead of snapping, so starts and
        // stops read as movement rather than teleporting.
        double accel = active.isOnGround() ? GameConfig.GROUND_ACCEL : GameConfig.AIR_ACCEL;
        if (dx != 0) {
            active.setVx(approach(active.getVx(), dx * GameConfig.MOVE_SPEED, accel * dt));
        } else {
            double brake = active.isOnGround() ? GameConfig.GROUND_FRICTION : GameConfig.AIR_ACCEL * 0.5;
            active.setVx(approach(active.getVx(), 0, brake * dt));
        }

        if (pressed.contains(KeyEvent.VK_UP) || pressed.contains(KeyEvent.VK_W)) {
            aimAngle = Math.min(GameConfig.MAX_ANGLE, aimAngle + GameConfig.AIM_RATE * dt);
        }
        if (pressed.contains(KeyEvent.VK_DOWN) || pressed.contains(KeyEvent.VK_S)) {
            aimAngle = Math.max(GameConfig.MIN_ANGLE, aimAngle - GameConfig.AIM_RATE * dt);
        }

        if (pressed.contains(KeyEvent.VK_F)) {
            charging = true;
            power = Math.min(GameConfig.MAX_POWER, power + GameConfig.CHARGE_RATE * dt);
        }

        turnTimeLeft -= dt;
        if (turnTimeLeft <= 0) {
            log(active.getName() + "'s turn timed out!");
            beginTurnTransition();
        }
    }

    private void updateCharacterPhysics(double dt) {
        Character active = getActive();
        for (Character c : allCharacters) {
            if (!c.isAlive()) continue;

            if (turnLocked && c == active) {
                c.setVx(approach(c.getVx(), 0, GameConfig.GROUND_FRICTION * dt));
            }

            double x = c.getX(), y = c.getY();

            Rectangle stuck = obstacleAt(x, y);
            if (stuck != null) {
                y = stuck.y - Character.HEIGHT;
                c.setVy(0);
            }

            c.setVy(c.getVy() + GameConfig.GRAVITY * dt);

            double nx = Math.max(0, Math.min(worldWidth - Character.WIDTH, x + c.getVx() * dt));
            Rectangle wall = obstacleAt(nx, y);
            if (wall != null) {
                double stepTop = wall.y - Character.HEIGHT;
                if (y - stepTop <= GameConfig.STEP_HEIGHT && obstacleAt(nx, stepTop) == null) {
                    y = stepTop;
                } else {
                    nx = x;
                    c.setVx(0);
                }
            }
            x = nx;

            double ny = y + c.getVy() * dt;
            boolean grounded = false;
            Rectangle surface = obstacleAt(x, ny);
            if (surface != null) {
                if (c.getVy() > 0) {
                    ny = surface.y - Character.HEIGHT;
                    grounded = true;
                } else {
                    ny = surface.y + surface.height;
                }
                c.setVy(0);
            } else if (ny + Character.HEIGHT >= groundY) {
                ny = groundY - Character.HEIGHT;
                c.setVy(0);
                grounded = true;
            }
            c.setOnGround(grounded);
            c.setPosition(x, ny);
        }
    }

    private void updateAnimators(double dt) {
        for (Character c : allCharacters) {
            if (!c.isAlive()) continue;
            Animator a = anim(c);
            a.update(c, dt);

            if (GameConfig.DUST_ENABLED) {
                if (a.consumeLanded()) {
                    spawnDust(c, GameConfig.DUST_ON_LANDING, 0);
                }
                if (a.consumeTurned()) {
                    spawnDust(c, GameConfig.DUST_ON_TURN, c.isFacingRight() ? -1 : 1);
                }
                if (a.consumeTookOff()) {
                    spawnDust(c, GameConfig.DUST_ON_TURN, 0);
                }
            }
        }
    }

    private void spawnDust(Character c, int count, double bias) {
        double cx = c.getX() + Character.WIDTH / 2.0;
        double feet = c.getY() + Character.HEIGHT;
        for (int i = 0; i < count; i++) {
            Dust d = new Dust();
            d.x = cx + (Math.random() - 0.5) * Character.WIDTH;
            d.y = feet - 2;
            double dir = bias != 0 ? bias : (Math.random() < 0.5 ? -1 : 1);
            d.vx = dir * Math.random() * GameConfig.DUST_SPEED;
            d.vy = -Math.random() * GameConfig.DUST_SPEED * 0.6;
            d.life = GameConfig.DUST_LIFETIME;
            dust.add(d);
        }
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.vy += GameConfig.GRAVITY * p.move.getGravityScale() * dt;
            double stepX = p.vx * dt;
            double stepY = p.vy * dt;
            p.x += stepX;
            p.y += stepY;
            p.travelled += Math.hypot(stepX, stepY);

            boolean hitGround = p.y >= groundY || projectileHitsObstacle(p);
            Character hitChar = null;
            for (Character c : allCharacters) {
                if (!c.isAlive() || c == p.owner) continue;
                double cx = c.getX() + Character.WIDTH / 2.0;
                double cy = c.getY() + Character.HEIGHT / 2.0;
                if (Math.hypot(p.x - cx, p.y - cy) < GameConfig.HIT_RADIUS) {
                    hitChar = c;
                    break;
                }
            }

            if (hitGround || hitChar != null) {
                explode(p, p.x, Math.min(p.y, groundY));
                it.remove();
            } else if (p.travelled >= maxRange() || p.x < -100 || p.x > worldWidth + 100) {
                it.remove();
            }
        }
    }

    private double maxRange() {
        return worldWidth * GameConfig.MAX_RANGE_FRACTION;
    }

    private boolean projectileHitsObstacle(Projectile p) {
        for (Rectangle r : obstacles) {
            if (r.contains(p.x, p.y)) return true;
        }
        return false;
    }

    private void explode(Projectile p, double x, double y) {
        Move move = p.move;
        double radius = Math.max(move.getSplashRadius(), GameConfig.HIT_RADIUS);
        double rangeScale = damageScaleForRange(p.travelled);

        for (Character c : allCharacters) {
            if (!c.isAlive()) continue;
            if (!GameConfig.FRIENDLY_FIRE && c.getPlayerId() == p.owner.getPlayerId()) continue;
            double cx = c.getX() + Character.WIDTH / 2.0;
            double cy = c.getY() + Character.HEIGHT / 2.0;
            double dist = Math.hypot(x - cx, y - cy);
            if (dist < radius) {
                double falloff = 1.0 - (dist / radius);
                int dmg = (int) Math.round(move.getDamage() * falloff * rangeScale);
                if (dmg > 0) {
                    c.takeDamage(dmg);
                    anim(c).triggerHit();
                    applyKnockback(c, p, x, y);
                }
            }
        }
        effects.add(new Fx(x, y, GameConfig.EXPLOSION_FX_SECONDS, 34));
        log(move.getName() + " hits!");
    }

    /** Full damage up close, tapering toward FALLOFF_MIN_DAMAGE at maximum range. */
    private double damageScaleForRange(double travelled) {
        if (!GameConfig.FALLOFF_ENABLED) return 1.0;
        double start = maxRange() * GameConfig.FALLOFF_START_FRACTION;
        if (travelled <= start) return 1.0;
        double t = Math.min(1, (travelled - start) / Math.max(1, maxRange() - start));
        return 1.0 - t * (1.0 - GameConfig.FALLOFF_MIN_DAMAGE);
    }

    private void applyKnockback(Character c, Projectile p, double x, double y) {
        double dirX = p.vx;
        double dirY = p.vy;
        double len = Math.hypot(dirX, dirY);
        if (len < 1) {
            dirX = (c.getX() + Character.WIDTH / 2.0) - x;
            dirY = (c.getY() + Character.HEIGHT / 2.0) - y;
            len = Math.max(1, Math.hypot(dirX, dirY));
        }
        c.setVx(c.getVx() + dirX / len * GameConfig.KNOCKBACK_SPEED);
        c.setVy(c.getVy() - Math.abs(dirY / len) * GameConfig.KNOCKBACK_SPEED * 0.4);
        c.setOnGround(false);
    }

    private void updateEffects(double dt) {
        Iterator<Fx> it = effects.iterator();
        while (it.hasNext()) {
            Fx fx = it.next();
            fx.life -= dt;
            if (fx.life <= 0) it.remove();
        }

        Iterator<Dust> dit = dust.iterator();
        while (dit.hasNext()) {
            Dust d = dit.next();
            d.life -= dt;
            d.x += d.vx * dt;
            d.y += d.vy * dt;
            d.vy += GameConfig.GRAVITY * 0.25 * dt;
            if (d.life <= 0) dit.remove();
        }
    }

    private void beginTurnTransition() {
        turnLocked = true;
        postActionTime = GameConfig.POST_ACTION_SECONDS;
    }

    private void nextTurn() {
        int p1Alive = 0, p2Alive = 0;
        for (Character c : allCharacters) {
            if (c.isAlive()) {
                if (c.getPlayerId() == 1) p1Alive++;
                else p2Alive++;
            }
        }
        if (p1Alive == 0 || p2Alive == 0) {
            gameOver = true;
            timer.stop();
            String msg;
            if (p1Alive == 0 && p2Alive == 0) msg = "IT'S A DRAW!";
            else if (p1Alive == 0) msg = "PLAYER 2 WINS!";
            else msg = "PLAYER 1 WINS!";
            if (onGameOver != null) onGameOver.accept(msg);
            return;
        }

        int attempts = 0;
        do {
            turnIndex = (turnIndex + 1) % turnOrder.size();
            attempts++;
        } while (!turnOrder.get(turnIndex).isAlive() && attempts <= turnOrder.size());

        aimAngle = 30;
        power = 0;
        charging = false;
        turnTimeLeft = GameConfig.TURN_SECONDS;
        turnLocked = false;
    }

    // ---- Input ----

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        boolean firstPress = !pressed.contains(code);
        pressed.add(code);
        if (initialized && !gameOver && firstPress && (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P)) {
            paused = !paused;
            pressed.clear();
            charging = false;
            return;
        }
        if (paused) {
            if (code == KeyEvent.VK_Q) quitToMenu();
            return;
        }
        if (turnLocked || !initialized || gameOver) return;

        if (firstPress && code == KeyEvent.VK_SPACE) doJump();
        if (firstPress && code == KeyEvent.VK_G) doAbility();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        pressed.remove(code);
        if (code == KeyEvent.VK_F && charging && !turnLocked && !paused) {
            doFire();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void quitToMenu() {
        timer.stop();
        gameOver = true;
        if (onQuit != null) onQuit.run();
    }

    private void doJump() {
        Character active = getActive();
        if (active != null && active.isOnGround()) {
            active.setVy(GameConfig.JUMP_VELOCITY);
            active.setOnGround(false);
        }
    }

    private void doFire() {
        charging = false;
        Character active = getActive();
        if (active == null) return;
        double p = Math.max(power, GameConfig.MIN_FIRE_POWER);
        spawnProjectile(active, active.getPrimaryMove(), aimAngle, p, 0);
        anim(active).triggerAttack(aimAngle);
        applyRecoil(active);
        power = 0;
        log(active.getName() + " fires " + active.getPrimaryMove().getName() + "!");
        beginTurnTransition();
    }

    private void applyRecoil(Character shooter) {
        double dir = shooter.isFacingRight() ? 1 : -1;
        shooter.setPosition(
                Math.max(0, Math.min(worldWidth - Character.WIDTH,
                        shooter.getX() - dir * GameConfig.RECOIL_PIXELS)),
                shooter.getY());
    }

    private void doAbility() {
        Character active = getActive();
        if (active == null) return;
        double p = power > 0 ? power : 60;
        active.useSpecialAbility(this, aimAngle, p);
        anim(active).triggerAttack(aimAngle);
        power = 0;
        charging = false;
        beginTurnTransition();
    }

    // ---- BattleContext ----

    @Override
    public List<Character> getAllCharacters() {
        return Collections.unmodifiableList(allCharacters);
    }

    @Override
    public double getWorldWidth() {
        return worldWidth;
    }

    @Override
    public double getGroundY() {
        return groundY;
    }

    @Override
    public void spawnProjectile(Character owner, Move move, double angleDegrees, double power, double angleOffsetDegrees) {
        double dir = owner.isFacingRight() ? 1 : -1;
        double effAngle = angleDegrees + angleOffsetDegrees;
        if (GameConfig.SPREAD_ENABLED) {
            effAngle += (Math.random() * 2 - 1) * GameConfig.SPREAD_DEGREES;
        }
        double rad = Math.toRadians(effAngle);
        double speed = (0.4 + 0.6 * Math.min(power, GameConfig.MAX_POWER) / 100.0)
                * move.getProjectileSpeed() * GameConfig.PROJECTILE_SPEED_SCALE;
        double vx = dir * speed * Math.cos(rad);
        double vy = -speed * Math.sin(rad);

        // Spawn at the hand, not the middle of the body.
        double[] hand = handPosition(owner, effAngle);
        projectiles.add(new Projectile(hand[0], hand[1], vx, vy, move, owner));
        effects.add(new Fx(hand[0], hand[1], GameConfig.MUZZLE_FLASH_SECONDS, 16));
    }

    /** Roughly where the weapon hand ends up once the attack pose has raised the arm. */
    private double[] handPosition(Character owner, double angleDegrees) {
        double dir = owner.isFacingRight() ? 1 : -1;
        double shoulderX = owner.getX() + Character.WIDTH / 2.0 + dir * (Character.WIDTH * 0.32);
        double shoulderY = owner.getY() + Character.HEIGHT * 0.30;
        double reach = Character.HEIGHT * 0.38;
        double rad = Math.toRadians(angleDegrees);
        return new double[]{
                shoulderX + dir * reach * Math.cos(rad),
                shoulderY - reach * Math.sin(rad)
        };
    }

    @Override
    public void log(String message) {
        lastLog = message;
        logTime = GameConfig.LOG_SECONDS;
    }

    // ---- Rendering ----

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int w = getWidth(), h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(135, 206, 235), 0, h, new Color(210, 235, 255)));
        g.fillRect(0, 0, w, h);

        int gY = (int) groundY;
        g.setColor(new Color(60, 140, 60));
        g.fillRect(0, gY, w, 10);
        g.setColor(new Color(92, 64, 34));
        g.fillRect(0, gY + 10, w, h - gY - 10);

        for (Rectangle r : obstacles) {
            g.setColor(new Color(120, 120, 130));
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setColor(new Color(60, 140, 60));
            g.fillRect(r.x, r.y, r.width, 6);
            g.setColor(new Color(50, 50, 58));
            g.drawRect(r.x, r.y, r.width, r.height);
        }

        for (Dust d : dust) {
            double k = Math.max(0, d.life / GameConfig.DUST_LIFETIME);
            g.setColor(new Color(190, 175, 150, (int) (160 * k)));
            int size = (int) (2 + 4 * k);
            g.fillOval((int) d.x - size / 2, (int) d.y - size / 2, size, size);
        }

        for (Character c : allCharacters) {
            drawCharacter(g, c, initialized && !gameOver && c == getActive());
        }

        for (Fx fx : effects) {
            double k = Math.max(0, fx.life / fx.maxLife);
            int radius = (int) (fx.size * k);
            g.setColor(new Color(255, 140, 0, (int) (180 * k)));
            g.fillOval((int) fx.x - radius / 2, (int) fx.y - radius / 2, radius, radius);
        }

        g.setColor(Color.YELLOW);
        for (Projectile p : projectiles) {
            g.fillOval((int) p.x - 4, (int) p.y - 4, 8, 8);
        }

        if (initialized && !gameOver && !turnLocked) {
            drawAimIndicator(g);
        }

        drawHud(g);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, w, h);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 56));
            drawCentered(g, "PAUSED", w / 2, h / 2 - 20);
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            drawCentered(g, "ESC / P  -  Resume", w / 2, h / 2 + 30);
            drawCentered(g, "Q  -  Quit to main menu", w / 2, h / 2 + 62);
        }
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y) {
        g.drawString(text, cx - g.getFontMetrics().stringWidth(text) / 2, y);
    }

    private void drawCharacter(Graphics2D g, Character c, boolean active) {
        int x = (int) c.getX(), y = (int) c.getY();

        if (!c.isAlive()) {
            g.setColor(Color.DARK_GRAY);
            g.setStroke(new BasicStroke(2));
            g.drawLine(x, y, x + Character.WIDTH, y + Character.HEIGHT);
            g.drawLine(x + Character.WIDTH, y, x, y + Character.HEIGHT);
            g.setStroke(new BasicStroke(1));
            return;
        }

        if (c.isShielded()) {
            g.setColor(new Color(120, 200, 255, 160));
            g.fillOval(x - 6, y - 6, Character.WIDTH + 12, Character.HEIGHT + 12);
        }

        CharacterSprite sprite = sprites.get(c.getClassLabel());
        if (sprite != null) {
            sprite.drawPosed(g, c.getX(), c.getY(), Character.WIDTH, Character.HEIGHT, c.isFacingRight(), anim(c));
        }

        int barW = 50, barH = 6;
        int bx = x + Character.WIDTH / 2 - barW / 2, by = y - 26;
        g.setColor(Color.RED.darker());
        g.fillRect(bx, by, barW, barH);
        g.setColor(Color.GREEN);
        int hw = (int) (barW * (c.getHealth() / (double) c.getMaxHealth()));
        g.fillRect(bx, by, hw, barH);
        g.setColor(Color.BLACK);
        g.drawRect(bx, by, barW, barH);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm2 = g.getFontMetrics();
        String label = c.getName();
        g.setColor(Color.WHITE);
        g.drawString(label, x + Character.WIDTH / 2 - fm2.stringWidth(label) / 2, by - 4);

        if (active) {
            int cx = x + Character.WIDTH / 2;
            int ty = by - 22;
            g.setColor(Color.YELLOW);
            g.fillPolygon(new int[]{cx - 8, cx + 8, cx}, new int[]{ty, ty, ty + 14}, 3);
        }
    }

    private void drawAimIndicator(Graphics2D g) {
        Character active = getActive();
        if (active == null || !active.isAlive()) return;

        double dir = active.isFacingRight() ? 1 : -1;
        double rad = Math.toRadians(aimAngle);
        int cx = (int) (active.getX() + Character.WIDTH / 2.0);
        int cy = (int) (active.getY() + Character.HEIGHT * 0.35);
        int len = 40;
        int ex = (int) (cx + dir * len * Math.cos(rad));
        int ey = (int) (cy - len * Math.sin(rad));

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx, cy, ex, ey);
        g.setStroke(new BasicStroke(1));

        int pbX = cx - 25, pbY = cy - 55, pbW = 50, pbH = 8;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(pbX, pbY, pbW, pbH);
        g.setColor(Color.ORANGE);
        g.fillRect(pbX, pbY, (int) (pbW * (power / GameConfig.MAX_POWER)), pbH);
        g.setColor(Color.BLACK);
        g.drawRect(pbX, pbY, pbW, pbH);
    }

    private void drawHud(Graphics2D g) {
        if (!gameOver && initialized) {
            Character active = getActive();
            if (active != null) {
                g.setFont(new Font("SansSerif", Font.BOLD, 22));
                g.setColor(Color.WHITE);
                g.drawString("Player " + active.getPlayerId() + " — " + active.getName() + " (" + active.getClassLabel() + ")", 20, 30);

                g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                int secs = Math.max(0, (int) Math.ceil(turnTimeLeft));
                g.drawString("Time: " + secs + "s", 20, 55);
            }
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("MOVE ←/→   JUMP SPACE   AIM ↑/↓   CHARGE+FIRE hold/release F   SPECIAL G   PAUSE ESC", 20, getHeight() - 15);

        if (logTime > 0 && lastLog != null && !lastLog.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(lastLog);
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(getWidth() / 2 - tw / 2 - 10, 20, tw + 20, 30, 8, 8);
            g.setColor(Color.YELLOW);
            g.drawString(lastLog, getWidth() / 2 - tw / 2, 42);
        }
    }
}
