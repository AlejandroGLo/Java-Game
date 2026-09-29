package characters;

import game.BattleContext;
import attacks.AttackIcons;
import attacks.MeleeAttack;
import attacks.AttackRange;
import attacks.Inventory;
import attacks.ProjectileAttack;
import attacks.SpecialAttack;
import game.CharacterClass;
import game.GameConfig;
import moves.Ability;
import moves.Move;

import java.awt.Color;

/**
 * Tank archetype. The full kit (Hammer Slam, Goblin Bomber, Regeneration) is
 * built in phase 4; for now it fights with a placeholder hammer throw so it is
 * selectable and playable.
 */
public class Orc extends Character {
    private static final Move PRIMARY = new Move("Hammer Slam", 26, 34, 9, 1.4);
    private static final Ability SPECIAL = new Ability("Regeneration", "Restores a portion of maximum health.");

    public Orc(String name, int playerId) {
        super(name, playerId, 130, new Color(106, 142, 68),
                GameConfig.TANK_BODY_WIDTH, GameConfig.TANK_BODY_HEIGHT);
    }


    private static final Move BOMBER = new Move("Goblin Bomber", 20, 46, 8, 1.1);

    @Override
    protected Inventory createInventory() {
        return new Inventory(
                new MeleeAttack("Hammer Slam",
                        "A slow overhead wind-up, then the great hammer crashes down in front of the orc. Hits harder than anything else.",
                        GameConfig.ORC_HAMMER_DAMAGE, GameConfig.ORC_HAMMER_RADIUS,
                        GameConfig.ORC_HAMMER_WINDUP, GameConfig.ORC_HAMMER_SWING,
                        GameConfig.ORC_HAMMER_KNOCKBACK, GameConfig.ORC_HAMMER_SHAKE,
                        AttackIcons.Kind.HAMMER),
                new ProjectileAttack("Goblin Bomber",
                        "Lobs a squealing goblin that detonates on contact, damaging a wide area.",
                        BOMBER, AttackRange.MEDIUM, AttackIcons.Kind.BOMB),
                new SpecialAttack("Regeneration",
                        "Knits flesh back together, restoring a portion of maximum health.",
                        0, AttackRange.MELEE, AttackIcons.Kind.HEAL, GameConfig.SPECIAL_MAX_USES));
    }

    @Override
    public CharacterClass getCharacterClass() {
        return CharacterClass.ORC;
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
        context.log(getName() + " regenerates!");
    }
}
