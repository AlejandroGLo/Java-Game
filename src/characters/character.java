package characters;

import game.BattleContext;
import game.CharacterClass;
import attacks.Inventory;
import game.GameConfig;
import moves.Ability;
import moves.Move;

import java.awt.Color;

public abstract class Character {

    protected final String name;
    protected final int playerId;
    protected final int maxHealth;
    protected final int width;
    protected final int height;
    protected int health;
    protected double x, y;
    protected double vx, vy;
    protected boolean onGround;
    protected boolean facingRight;
    protected boolean shielded;
    protected final Color color;
    private Inventory inventory;

    protected Character(String name, int playerId, int maxHealth, Color color) {
        this(name, playerId, maxHealth, color, GameConfig.BODY_WIDTH, GameConfig.BODY_HEIGHT);
    }

    protected Character(String name, int playerId, int maxHealth, Color color, int width, int height) {
        this.name = name;
        this.playerId = playerId;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.color = color;
        this.width = width;
        this.height = height;
        this.facingRight = playerId == 1;
    }

    /**
     * Identity of this character. Sprites, archetype and the display label are all
     * derived from this, so there is a single source of truth: a new character
     * cannot be added without the compiler forcing this to be answered.
     */
    public abstract CharacterClass getCharacterClass();

    public final String getClassLabel() {
        return getCharacterClass().getLabel();
    }

    /** Built lazily: subclass fields must be initialised before this runs. */
    public final Inventory getInventory() {
        if (inventory == null) inventory = createInventory();
        return inventory;
    }

    protected abstract Inventory createInventory();

    public abstract Move getPrimaryMove();

    public abstract Ability getSpecialAbility();

    public abstract void useSpecialAbility(BattleContext context, double aimAngleDegrees, double power);

    public void takeDamage(int damage) {
        int actual = shielded ? Math.max(1, damage / 2) : damage;
        shielded = false;
        health = Math.max(0, health - actual);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public String getName() {
        return name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    /** Collision box width. Tanks are bulkier than everyone else. */
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isShielded() {
        return shielded;
    }

    public void setShielded(boolean shielded) {
        this.shielded = shielded;
    }

    public Color getColor() {
        return color;
    }
}
