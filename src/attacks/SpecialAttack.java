package attacks;

import characters.Character;
import game.BattleContext;

import java.awt.Graphics2D;

/**
 * A limited-use special. The effect itself lives on the character, so each class
 * keeps ownership of its own signature move.
 */
public class SpecialAttack implements Attack {

    private final String name;
    private final String description;
    private final int baseDamage;
    private final AttackRange range;
    private final AttackIcons.Kind icon;
    private final int maxUses;

    public SpecialAttack(String name, String description, int baseDamage,
                         AttackRange range, AttackIcons.Kind icon, int maxUses) {
        this.name = name;
        this.description = description;
        this.baseDamage = baseDamage;
        this.range = range;
        this.icon = icon;
        this.maxUses = maxUses;
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
        return baseDamage;
    }

    @Override
    public AttackRange getRange() {
        return range;
    }

    @Override
    public int getMaxUses() {
        return maxUses;
    }

    @Override
    public void drawIcon(Graphics2D g, int x, int y, int size) {
        AttackIcons.draw(g, icon, x, y, size);
    }

    @Override
    public void execute(BattleContext context, Character user, double aimAngleDegrees, double power) {
        user.useSpecialAbility(context, aimAngleDegrees, power);
    }
}
