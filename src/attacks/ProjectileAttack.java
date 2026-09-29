package attacks;

import characters.Character;
import game.BattleContext;
import moves.Move;

import java.awt.Graphics2D;

/**
 * An attack that launches one or more projectiles. Covers everything from a
 * single fireball to a fanned volley by varying shot count and spread.
 */
public class ProjectileAttack implements Attack {

    private final String name;
    private final String description;
    private final Move move;
    private final AttackRange range;
    private final AttackIcons.Kind icon;
    private final int shots;
    private final double spreadDegrees;

    public ProjectileAttack(String name, String description, Move move,
                            AttackRange range, AttackIcons.Kind icon) {
        this(name, description, move, range, icon, 1, 0);
    }

    public ProjectileAttack(String name, String description, Move move,
                            AttackRange range, AttackIcons.Kind icon,
                            int shots, double spreadDegrees) {
        this.name = name;
        this.description = description;
        this.move = move;
        this.range = range;
        this.icon = icon;
        this.shots = shots;
        this.spreadDegrees = spreadDegrees;
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
        return move.getDamage();
    }

    @Override
    public AttackRange getRange() {
        return range;
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
        if (shots <= 1) {
            context.spawnProjectile(user, move, aimAngleDegrees, power, 0);
        } else {
            // Fan the shots evenly around the aim line.
            double step = spreadDegrees / (shots - 1);
            double start = -spreadDegrees / 2;
            for (int i = 0; i < shots; i++) {
                context.spawnProjectile(user, move, aimAngleDegrees, power, start + i * step);
            }
        }
        context.log(user.getName() + " uses " + name + "!");
    }
}
