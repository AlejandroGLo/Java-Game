package characters;

import game.BattleContext;
import moves.Ability;
import moves.Move;

import java.awt.Color;

public abstract class Character {
    public static final int WIDTH = 30;
    public static final int HEIGHT = 46;

    protected final String name;
    protected final int playerId;
    protected final int maxHealth;
    protected int health;
    protected double x, y;
    protected double vx, vy;
    protected boolean onGround;
    protected boolean facingRight;
    protected boolean shielded;
    protected final Color color;

    protected Character(String name, int playerId, int maxHealth, Color color) {
        this.name = name;
        this.playerId = playerId;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.color = color;
        this.facingRight = playerId == 1;
    }

    public abstract String getClassLabel();

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
