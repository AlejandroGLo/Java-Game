package attacks;

import characters.Character;
import game.BattleContext;

import java.awt.Graphics2D;

/**
 * A weapon swing rather than a projectile.
 *
 * The weapon sweeps through the half-circle directly in front of the character,
 * from overhead down to the ground, and damages everything the arc passes
 * through. The swing itself is simulated by the battle screen; this class only
 * describes its shape, timing and damage.
 */
public class MeleeAttack implements Attack {

    private final String name;
    private final String description;
    private final int damage;
    private final double radius;
    private final double windupSeconds;
    private final double swingSeconds;
    private final double knockback;
    private final double screenShake;
    private final AttackIcons.Kind icon;

    public MeleeAttack(String name, String description, int damage, double radius,
                       double windupSeconds, double swingSeconds, double knockback,
                       double screenShake, AttackIcons.Kind icon) {
        this.name = name;
        this.description = description;
        this.damage = damage;
        this.radius = radius;
        this.windupSeconds = windupSeconds;
        this.swingSeconds = swingSeconds;
        this.knockback = knockback;
        this.screenShake = screenShake;
        this.icon = icon;
    }

    /** Reach of the arc, measured from the shoulder pivot. */
    public double getRadius() {
        return radius;
    }

    /** Time spent raising the weapon before the arc becomes dangerous. */
    public double getWindupSeconds() {
        return windupSeconds;
    }

    /** Time the weapon takes to sweep the full half-circle. */
    public double getSwingSeconds() {
        return swingSeconds;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getScreenShake() {
        return screenShake;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getBaseDamage() {
        return damage;
    }

    @Override
    public AttackRange getRange() {
        return AttackRange.MELEE;
    }

    @Override
    public int getMaxUses() {
        return UNLIMITED;
    }

    @Override
    public void drawIcon(Graphics2D g, int x, int y, int size) {
        AttackIcons.draw(g, icon, x, y, size);
    }

    @Override
    public void execute(BattleContext context, Character user, double aimAngleDegrees, double power) {
        context.startMeleeSwing(user, this);
    }
}
