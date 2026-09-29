package characters;

import game.BattleContext;
import attacks.AttackIcons;
import attacks.AttackRange;
import attacks.Inventory;
import attacks.ProjectileAttack;
import attacks.SpecialAttack;
import game.CharacterClass;
import game.GameConfig;
import moves.Ability;
import moves.Move;

import java.awt.Color;

public class Archer extends Character {
    private static final Move PRIMARY = new Move("Arrow Shot", 20, 12, 15, 0.7);
    private static final Ability SPECIAL = new Ability("Volley", "Fires three arrows in a spread instead of one.");

    public Archer(String name, int playerId) {
        super(name, playerId, 90, new Color(70, 160, 90));
    }


    @Override
    protected Inventory createInventory() {
        return new Inventory(
                new ProjectileAttack("Arrow Shot",
                        "A single fast arrow on a flat arc. Precise, with almost no splash.",
                        PRIMARY, AttackRange.LONG, AttackIcons.Kind.ARROW),
                new ProjectileAttack("Volley",
                        "Looses three arrows in a spread. Close up several can land at once.",
                        PRIMARY, AttackRange.MEDIUM, AttackIcons.Kind.ARROW, 3, 28),
                new SpecialAttack("Burning Arrow",
                        "Sets the next arrow alight, burning whatever it hits.",
                        0, AttackRange.LONG, AttackIcons.Kind.FIREBALL, GameConfig.SPECIAL_MAX_USES));
    }

    @Override
    public CharacterClass getCharacterClass() {
        return CharacterClass.ARCHER;
    }

    @Override
    public Move getPrimaryMove() {
        return PRIMARY;
    }

    @Override
    public Ability getSpecialAbility() {
        return SPECIAL;
    }

    @Override
    public void useSpecialAbility(BattleContext context, double aimAngleDegrees, double power) {
        double p = Math.max(power, 50);
        context.spawnProjectile(this, PRIMARY, aimAngleDegrees, p, -14);
        context.spawnProjectile(this, PRIMARY, aimAngleDegrees, p, 0);
        context.spawnProjectile(this, PRIMARY, aimAngleDegrees, p, 14);
        context.log(getName() + " looses a volley of arrows!");
    }
}
