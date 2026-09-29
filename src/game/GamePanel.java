package game;

import attacks.Attack;
import attacks.Inventory;
import attacks.MeleeAttack;
import characters.Character;
import moves.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final Map<Character, Animator> animators = new HashMap<>();

    private double worldWidth = 1200;
    private double groundY = 620;
    private boolean initialized = false;
    private boolean gameOver = false;
    private boolean turnLocked = false;
    private boolean charging = false;
    private boolean paused = false;
    private boolean inventoryOpen = false;
    private int menuCursor = 0;
    private Point menuMouse = new Point(-1, -1);
    private MeleeSwing activeSwing;
    private final List<MeleeSwing> fadingSwings = new ArrayList<>();
    private double shakeTime;
    private double shakeMagnitude;
    private boolean debugHitboxes = GameConfig.DEBUG_HITBOXES;
    private final Rectangle[] menuSlotRect = {new Rectangle(), new Rectangle(), new Rectangle()};
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

        for (Character c : allCharacters) {
            animators.put(c, new Animator());
        }

        setPreferredSize(new Dimension(1200, 700));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (!inventoryOpen) return;
                for (int i = 0; i < Inventory.SLOTS; i++) {
                    if (menuSlotRect[i].contains(e.getPoint())) {
                        menuCursor = i;
                        confirmSlot(i);
                        return;
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                menuMouse = e.getPoint();
            }
        });

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
        updateMeleeSwing(dt);
        updateEffects(dt);

        if (turnLocked && projectiles.isEmpty() && activeSwing == null) {
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

    private Rectangle obstacleAt(double x, double y, Character c) {
        return obstacleAt(x, y, c.getWidth(), c.getHeight());
    }

    private Rectangle obstacleAt(double x, double y, int boxW, int boxH) {
        for (Rectangle r : obstacles) {
            if (x < r.x + r.width && x + boxW > r.x
                    && y < r.y + r.height && y + boxH > r.y) {
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
                c.setPosition(fx, groundY - c.getHeight());
                c.setFacingRight(true);
                i1++;
            } else {
                double fx = worldWidth * (0.88 - 0.10 * i2) - c.getWidth();
                c.setPosition(fx, groundY - c.getHeight());
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

        // While the menu is open the character can neither move nor attack.
        if (inventoryOpen) {
            active.setVx(approach(active.getVx(), 0, GameConfig.GROUND_FRICTION * dt));
            if (!GameConfig.INVENTORY_PAUSES_TURN_TIMER) tickTurnClock(active, dt);
            return;
        }

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

        tickTurnClock(active, dt);
    }

    private void tickTurnClock(Character active, double dt) {
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

            Rectangle stuck = obstacleAt(x, y, c);
            if (stuck != null) {
                y = stuck.y - c.getHeight();
                c.setVy(0);
            }

            c.setVy(c.getVy() + GameConfig.GRAVITY * dt);

            double nx = Math.max(0, Math.min(worldWidth - c.getWidth(), x + c.getVx() * dt));
            Rectangle wall = obstacleAt(nx, y, c);
            if (wall != null) {
                double stepTop = wall.y - c.getHeight();
                if (y - stepTop <= GameConfig.STEP_HEIGHT && obstacleAt(nx, stepTop, c) == null) {
                    y = stepTop;
                } else {
                    nx = x;
                    c.setVx(0);
                }
            }
            x = nx;

            double ny = y + c.getVy() * dt;
            boolean grounded = false;
            Rectangle surface = obstacleAt(x, ny, c);
            if (surface != null) {
                if (c.getVy() > 0) {
                    ny = surface.y - c.getHeight();
                    grounded = true;
                } else {
                    ny = surface.y + surface.height;
                }
                c.setVy(0);
            } else if (ny + c.getHeight() >= groundY) {
                ny = groundY - c.getHeight();
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
        double cx = c.getX() + c.getWidth() / 2.0;
        double feet = c.getY() + c.getHeight();
        for (int i = 0; i < count; i++) {
            Dust d = new Dust();
            d.x = cx + (Math.random() - 0.5) * c.getWidth();
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
                double cx = c.getX() + c.getWidth() / 2.0;
                double cy = c.getY() + c.getHeight() / 2.0;
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

    @Override
    public void startMeleeSwing(Character user, MeleeAttack attack) {
        activeSwing = new MeleeSwing(user, attack);
        anim(user).triggerSwing(attack.getWindupSeconds(), attack.getSwingSeconds());
        log(user.getName() + " swings " + attack.getName() + "!");
    }

    private void updateMeleeSwing(double dt) {
        Iterator<MeleeSwing> fading = fadingSwings.iterator();
        while (fading.hasNext()) {
            MeleeSwing s = fading.next();
            s.elapsed += dt;
            if (s.elapsed > s.totalSeconds() + GameConfig.MELEE_TRAIL_SECONDS) fading.remove();
        }

        if (activeSwing == null) return;
        MeleeSwing swing = activeSwing;
        swing.elapsed += dt;

        if (swing.isActive()) {
            for (Character target : allCharacters) {
                if (!target.isAlive() || target == swing.owner) continue;
                if (!GameConfig.FRIENDLY_FIRE && target.getPlayerId() == swing.owner.getPlayerId()) continue;
                if (swing.alreadyHit.contains(target)) continue;
                if (!swing.covers(target)) continue;

                swing.alreadyHit.add(target);
                applyMeleeHit(swing, target);
            }
        }

        if (swing.isFinished()) {
            fadingSwings.add(swing);
            activeSwing = null;
        }
    }

    private void applyMeleeHit(MeleeSwing swing, Character target) {
        MeleeAttack attack = swing.attack;
        target.takeDamage(attack.getBaseDamage());
        anim(target).triggerHit();

        double dir = swing.facingRight ? 1 : -1;
        target.setVx(target.getVx() + dir * attack.getKnockback());
        target.setVy(target.getVy() - attack.getKnockback() * 0.35);
        target.setOnGround(false);

        effects.add(new Fx(target.getX() + target.getWidth() / 2.0,
                target.getY() + target.getHeight() / 2.0,
                GameConfig.EXPLOSION_FX_SECONDS * 0.6, 26));

        if (attack.getScreenShake() > 0) {
            shakeTime = GameConfig.SCREEN_SHAKE_SECONDS;
            shakeMagnitude = attack.getScreenShake();
        }
        log(attack.getName() + " hits " + target.getName() + "!");
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
            double cx = c.getX() + c.getWidth() / 2.0;
            double cy = c.getY() + c.getHeight() / 2.0;
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
            dirX = (c.getX() + c.getWidth() / 2.0) - x;
            dirY = (c.getY() + c.getHeight() / 2.0) - y;
            len = Math.max(1, Math.hypot(dirX, dirY));
        }
        c.setVx(c.getVx() + dirX / len * GameConfig.KNOCKBACK_SPEED);
        c.setVy(c.getVy() - Math.abs(dirY / len) * GameConfig.KNOCKBACK_SPEED * 0.4);
        c.setOnGround(false);
    }

    private void updateEffects(double dt) {
        if (shakeTime > 0) shakeTime = Math.max(0, shakeTime - dt);
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
        inventoryOpen = false;
        turnTimeLeft = GameConfig.TURN_SECONDS;
        turnLocked = false;

        Character next = getActive();
        if (next != null) next.getInventory().resetForTurn();
    }

    // ---- Input ----

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        boolean firstPress = !pressed.contains(code);
        pressed.add(code);

        if (firstPress && code == KeyEvent.VK_F3) {
            debugHitboxes = !debugHitboxes;
            log(debugHitboxes ? "Hitbox debug ON" : "Hitbox debug OFF");
            return;
        }

        // The menu owns Esc while it is open, so it closes rather than pausing.
        if (inventoryOpen) {
            if (firstPress) handleInventoryKey(code);
            return;
        }
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

        if (firstPress && code == KeyEvent.VK_E) {
            openInventory();
            return;
        }
        if (firstPress && code >= KeyEvent.VK_1 && code <= KeyEvent.VK_3) {
            confirmSlot(code - KeyEvent.VK_1);
            return;
        }
        if (firstPress && code == KeyEvent.VK_SPACE) doJump();
        if (firstPress && code == KeyEvent.VK_G) doAbility();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        pressed.remove(code);
        if (code == KeyEvent.VK_F && charging && !turnLocked && !paused && !inventoryOpen) {
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

    private void openInventory() {
        Character active = getActive();
        if (active == null) return;
        inventoryOpen = true;
        menuCursor = active.getInventory().getSelectedSlot();
        charging = false;
        power = 0;
        // Drop held keys so movement doesn't resume when the menu closes.
        pressed.clear();
    }

    private void closeInventory() {
        inventoryOpen = false;
        pressed.clear();
    }

    private void handleInventoryKey(int code) {
        switch (code) {
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_E:
                closeInventory();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                menuCursor = (menuCursor + Inventory.SLOTS - 1) % Inventory.SLOTS;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                menuCursor = (menuCursor + 1) % Inventory.SLOTS;
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                confirmSlot(menuCursor);
                break;
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
                confirmSlot(code - KeyEvent.VK_1);
                break;
            default:
                break;
        }
    }

    /** Selects a slot if it is still usable, then closes the menu. */
    private void confirmSlot(int slot) {
        Character active = getActive();
        if (active == null || turnLocked) return;
        Inventory inv = active.getInventory();
        if (inv.select(slot)) {
            log(active.getName() + " readies " + inv.getSelected().getName());
            closeInventory();
        } else {
            Attack a = inv.get(slot);
            log((a == null ? "That slot" : a.getName()) + " has no uses left!");
        }
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
        Inventory inv = active.getInventory();
        int slot = inv.getSelectedSlot();
        Attack attack = inv.getSelected();
        if (attack == null || !inv.isUsable(slot)) return;

        double p = Math.max(power, GameConfig.MIN_FIRE_POWER);
        attack.execute(this, active, aimAngle, p);
        inv.consume(slot);
        anim(active).triggerAttack(aimAngle);
        applyRecoil(active);
        power = 0;
        beginTurnTransition();
    }

    private void applyRecoil(Character shooter) {
        double dir = shooter.isFacingRight() ? 1 : -1;
        shooter.setPosition(
                Math.max(0, Math.min(worldWidth - shooter.getWidth(),
                        shooter.getX() - dir * GameConfig.RECOIL_PIXELS)),
                shooter.getY());
    }

    /** G remains a direct shortcut to the special, bypassing the menu. */
    private void doAbility() {
        Character active = getActive();
        if (active == null) return;
        Inventory inv = active.getInventory();
        if (!inv.isUsable(Inventory.SLOT_SPECIAL)) {
            log(active.getName() + "'s special is used up!");
            return;
        }
        inv.select(Inventory.SLOT_SPECIAL);
        double p = power > 0 ? power : 60;
        inv.get(Inventory.SLOT_SPECIAL).execute(this, active, aimAngle, p);
        inv.consume(Inventory.SLOT_SPECIAL);
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
        double shoulderX = owner.getX() + owner.getWidth() / 2.0 + dir * (owner.getWidth() * 0.32);
        double shoulderY = owner.getY() + owner.getHeight() * 0.30;
        double reach = owner.getHeight() * 0.38;
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

        double shakeX = 0, shakeY = 0;
        if (shakeTime > 0) {
            double k = shakeTime / GameConfig.SCREEN_SHAKE_SECONDS;
            shakeX = (Math.random() * 2 - 1) * shakeMagnitude * k;
            shakeY = (Math.random() * 2 - 1) * shakeMagnitude * k;
        }
        g.translate(shakeX, shakeY);

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

        drawMeleeSwings(g);

        if (initialized && !gameOver && !turnLocked) {
            drawAimIndicator(g);
        }
        if (debugHitboxes) {
            drawDebugHitboxes(g);
        }

        g.translate(-shakeX, -shakeY);

        drawHud(g);
        drawSelectedAttack(g);

        if (inventoryOpen) {
            drawInventoryMenu(g);
        }

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
            g.drawLine(x, y, x + c.getWidth(), y + c.getHeight());
            g.drawLine(x + c.getWidth(), y, x, y + c.getHeight());
            g.setStroke(new BasicStroke(1));
            return;
        }

        if (c.isShielded()) {
            g.setColor(new Color(120, 200, 255, 160));
            g.fillOval(x - 6, y - 6, c.getWidth() + 12, c.getHeight() + 12);
        }

        CharacterSprite sprite = Sprites.forCharacter(c);
        if (sprite != null) {
            sprite.drawPosed(g, c.getX(), c.getY(), c.getWidth(), c.getHeight(), c.isFacingRight(), anim(c));
        } else if (GameConfig.SPRITE_PLACEHOLDER_ENABLED) {
            drawSpritePlaceholder(g, c, x, y);
        }

        int barW = 50, barH = 6;
        int bx = x + c.getWidth() / 2 - barW / 2, by = y - 26;
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
        g.drawString(label, x + c.getWidth() / 2 - fm2.stringWidth(label) / 2, by - 4);

        if (active) {
            int cx = x + c.getWidth() / 2;
            int ty = by - 22;
            g.setColor(Color.YELLOW);
            g.fillPolygon(new int[]{cx - 8, cx + 8, cx}, new int[]{ty, ty, ty + 14}, 3);
        }
    }

    /** Loud stand-in so a missing sprite is obvious on screen rather than invisible. */
    private void drawSpritePlaceholder(Graphics2D g, Character c, int x, int y) {
        g.setColor(c.getColor());
        g.fillRect(x, y, c.getWidth(), c.getHeight());
        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, c.getWidth(), c.getHeight());
        g.drawLine(x, y, x + c.getWidth(), y + c.getHeight());
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        String label = c.getClassLabel();
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(label, x + c.getWidth() / 2 - fm.stringWidth(label) / 2,
                y + c.getHeight() / 2 + 4);
    }


    /** Always-visible reminder of what pressing F will do. */
    private void drawSelectedAttack(Graphics2D g) {
        if (!initialized || gameOver) return;
        Character active = getActive();
        if (active == null || !active.isAlive()) return;
        Inventory inv = active.getInventory();
        Attack attack = inv.getSelected();
        if (attack == null) return;

        int pad = 10, icon = 30;
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        int textW = g.getFontMetrics().stringWidth(attack.getName());
        int boxW = pad * 3 + icon + textW;
        int boxH = icon + pad * 2;
        int bx = 20, by = getHeight() - 46 - boxH;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(bx, by, boxW, boxH, 10, 10);
        g.setColor(new Color(150, 160, 185));
        g.drawRoundRect(bx, by, boxW, boxH, 10, 10);

        attack.drawIcon(g, bx + pad, by + pad, icon);
        g.setColor(Color.WHITE);
        g.drawString(attack.getName(), bx + pad * 2 + icon, by + pad + icon / 2 + 5);

        if (inv.isLimited(inv.getSelectedSlot())) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(255, 210, 120));
            g.drawString(inv.getUsesLeft(inv.getSelectedSlot()) + " left",
                    bx + pad * 2 + icon, by + boxH - 6);
        }
    }

    private void drawInventoryMenu(Graphics2D g) {
        Character active = getActive();
        if (active == null) return;
        Inventory inv = active.getInventory();

        int w = getWidth(), h = getHeight();
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, 0, w, h);

        int sw = GameConfig.MENU_SLOT_WIDTH, sh = GameConfig.MENU_SLOT_HEIGHT;
        int gap = GameConfig.MENU_SLOT_GAP;
        int totalW = sw * Inventory.SLOTS + gap * (Inventory.SLOTS - 1);
        int x0 = w / 2 - totalW / 2;
        int y0 = h / 2 - sh / 2;

        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.setColor(Color.WHITE);
        drawCentered(g, active.getName() + "  \u2014  " + active.getClassLabel(), w / 2, y0 - 34);

        String[] slotTitles = {"ATTACK 1", "ATTACK 2", "SPECIAL"};
        for (int i = 0; i < Inventory.SLOTS; i++) {
            menuSlotRect[i].setBounds(x0 + i * (sw + gap), y0, sw, sh);
            drawInventorySlot(g, inv, i, slotTitles[i]);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(170, 176, 195));
        drawCentered(g, "\u2190/\u2192 or A/D select    ENTER confirm    1/2/3 quick-pick    E or ESC close",
                w / 2, y0 + sh + 38);
    }

    private void drawInventorySlot(Graphics2D g, Inventory inv, int i, String title) {
        Rectangle r = menuSlotRect[i];
        Attack attack = inv.get(i);
        boolean usable = inv.isUsable(i);
        boolean cursor = i == menuCursor || r.contains(menuMouse);
        boolean equipped = i == inv.getSelectedSlot();

        g.setColor(usable ? new Color(28, 31, 44) : new Color(20, 21, 27));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setStroke(new BasicStroke(cursor ? 3f : 1.5f));
        g.setColor(!usable ? new Color(70, 72, 84)
                : cursor ? new Color(255, 214, 96)
                : equipped ? new Color(120, 200, 140) : new Color(64, 68, 88));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setStroke(new BasicStroke(1f));

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(140, 148, 172));
        g.drawString(title, r.x + 14, r.y + 22);
        if (equipped && usable) {
            g.setColor(new Color(120, 200, 140));
            String tag = "EQUIPPED";
            g.drawString(tag, r.x + r.width - 14 - g.getFontMetrics().stringWidth(tag), r.y + 22);
        }

        if (attack == null) return;

        int icon = GameConfig.MENU_ICON_SIZE;
        int ix = r.x + r.width / 2 - icon / 2;
        int iy = r.y + 34;
        Composite oldComposite = g.getComposite();
        if (!usable) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        attack.drawIcon(g, ix, iy, icon);
        g.setComposite(oldComposite);

        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.setColor(usable ? Color.WHITE : new Color(120, 124, 140));
        drawCentered(g, attack.getName(), r.x + r.width / 2, iy + icon + 24);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(usable ? new Color(178, 184, 204) : new Color(100, 104, 120));
        int ty = iy + icon + 44;
        for (String line : wrap(g, attack.getDescription(), r.width - 28)) {
            drawCentered(g, line, r.x + r.width / 2, ty);
            ty += 15;
        }

        int sy = r.y + r.height - 44;
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(150, 156, 178));
        String dmg = attack.getBaseDamage() > 0 ? String.valueOf(attack.getBaseDamage()) : "\u2014";
        g.drawString("Damage", r.x + 14, sy);
        g.drawString("Range", r.x + 14, sy + 16);
        g.setColor(usable ? Color.WHITE : new Color(120, 124, 140));
        String rangeLabel = attack.getRange().getLabel();
        g.drawString(dmg, r.x + r.width - 14 - g.getFontMetrics().stringWidth(dmg), sy);
        g.drawString(rangeLabel, r.x + r.width - 14 - g.getFontMetrics().stringWidth(rangeLabel), sy + 16);

        if (inv.isLimited(i)) {
            g.setColor(new Color(150, 156, 178));
            g.drawString("Uses", r.x + 14, sy + 32);
            String uses = usable ? (inv.getUsesLeft(i) + " / " + attack.getMaxUses()) : "Used";
            g.setColor(usable ? new Color(255, 210, 120) : new Color(220, 96, 96));
            g.drawString(uses, r.x + r.width - 14 - g.getFontMetrics().stringWidth(uses), sy + 32);
        }

        if (!usable) {
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.setColor(new Color(220, 96, 96));
            drawCentered(g, "USED", r.x + r.width / 2, iy + icon / 2 + 8);
        }
    }

    private java.util.List<String> wrap(Graphics2D g, String text, int maxWidth) {
        java.util.List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (g.getFontMetrics().stringWidth(candidate) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    /** Fading wedge showing the ground the weapon has swept. */
    private void drawMeleeSwings(Graphics2D g) {
        if (activeSwing != null) drawSwing(g, activeSwing);
        for (MeleeSwing swing : fadingSwings) drawSwing(g, swing);
    }

    private void drawSwing(Graphics2D g, MeleeSwing swing) {
        double past = swing.elapsed - swing.totalSeconds();
        double fade = past <= 0 ? 1 : Math.max(0, 1 - past / GameConfig.MELEE_TRAIL_SECONDS);
        if (fade <= 0) return;

        int r = (int) swing.attack.getRadius();
        int cx = (int) swing.pivotX();
        int cy = (int) swing.pivotY();

        // Java2D angles run counter-clockwise from 3 o'clock; the arc starts
        // overhead (90 degrees) and sweeps down through the facing side.
        double swept = swing.sweepProgress() * 180;
        int start = 90;
        int extent = (int) (swing.facingRight ? -swept : swept);

        if (Math.abs(extent) > 1) {
            // A thick band along the outer edge reads as a swipe; a full pie
            // wedge from the pivot just looks like a spotlight.
            int band = (int) (r * 0.74);
            float thickness = (float) (r * 0.42);

            g.setStroke(new BasicStroke(thickness + 4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(70, 60, 30, (int) (70 * fade)));
            g.drawArc(cx - band, cy - band, band * 2, band * 2, start, extent);

            g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 240, 170, (int) (130 * fade)));
            g.drawArc(cx - band, cy - band, band * 2, band * 2, start, extent);

            g.setStroke(new BasicStroke(Math.max(2.5f, (float) (3.5 * fade))));
            g.setColor(new Color(255, 255, 255, (int) (235 * fade)));
            g.drawArc(cx - r, cy - r, r * 2, r * 2, start, extent);
            g.setStroke(new BasicStroke(1f));
        }

        // Leading edge of the weapon.
        if (swing.isActive()) {
            double angle = Math.toRadians(start + extent);
            int ex = (int) (cx + Math.cos(angle) * r);
            int ey = (int) (cy - Math.sin(angle) * r);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(60, 52, 28, 150));
            g.drawLine(cx, cy, ex, ey);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 245, 245));
            g.drawLine(cx, cy, ex, ey);
            g.setColor(new Color(255, 250, 220, 220));
            g.fillOval(ex - 4, ey - 4, 8, 8);
            g.setStroke(new BasicStroke(1f));
        }
    }

    private void drawDebugHitboxes(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        for (Character c : allCharacters) {
            if (!c.isAlive()) continue;
            g.setColor(c.getPlayerId() == 1 ? new Color(120, 200, 255) : new Color(255, 150, 150));
            g.drawRect((int) c.getX(), (int) c.getY(), c.getWidth(), c.getHeight());
            g.drawLine((int) (c.getX() + c.getWidth() / 2.0) - 3, (int) (c.getY() + c.getHeight() / 2.0),
                    (int) (c.getX() + c.getWidth() / 2.0) + 3, (int) (c.getY() + c.getHeight() / 2.0));
        }

        MeleeSwing swing = activeSwing;
        if (swing != null) {
            int r = (int) swing.attack.getRadius();
            int cx = (int) swing.pivotX();
            int cy = (int) swing.pivotY();
            g.setColor(new Color(255, 80, 80, 90));
            g.fillArc(cx - r, cy - r, r * 2, r * 2, 90, swing.facingRight ? -180 : 180);
            g.setColor(new Color(255, 60, 60));
            g.drawArc(cx - r, cy - r, r * 2, r * 2, 90, swing.facingRight ? -180 : 180);
            g.fillOval(cx - 3, cy - 3, 6, 6);
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(255, 120, 120));
        g.drawString("F3 hitbox debug", 20, 80);
    }

    private void drawAimIndicator(Graphics2D g) {
        Character active = getActive();
        if (active == null || !active.isAlive()) return;

        double dir = active.isFacingRight() ? 1 : -1;
        double rad = Math.toRadians(aimAngle);
        int cx = (int) (active.getX() + active.getWidth() / 2.0);
        int cy = (int) (active.getY() + active.getHeight() * 0.35);
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
        g.drawString("MOVE ←/→   JUMP SPACE   AIM ↑/↓   CHARGE+FIRE hold/release F   INVENTORY E   QUICK-PICK 1/2/3   SPECIAL G   PAUSE ESC",
                20, getHeight() - 15);

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
