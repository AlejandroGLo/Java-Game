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

public class Knight extends Character {
    private static final Move PRIMARY = new Move("Hammer Throw", 28, 30, 11, 1.3);
    private static final Ability SPECIAL = new Ability("Shield Block", "Raises a shield that halves the next hit taken.");

    public Knight(String name, int playerId) {
        super(name, playerId, 130, new Color(150, 150, 170));
    }


    private static final Move THROWN = new Move("Sword Throw", 17, 18, 13, 1.0);

    @Override
    protected Inventory createInventory() {
        return new Inventory(
                new ProjectileAttack("Sword Strike",
                        "A heavy swing of the knight's blade. Reliable damage at close quarters.",
                        PRIMARY, AttackRange.SHORT, AttackIcons.Kind.SWORD),
                new ProjectileAttack("Sword Throw",
                        "Hurls the sword end over end. Travels further than the swing but hits for less.",
                        THROWN, AttackRange.MEDIUM, AttackIcons.Kind.SWORD_THROWN),
                new SpecialAttack("Shield",
                        "Raises a shield that absorbs the next hit taken.",
                        0, AttackRange.MELEE, AttackIcons.Kind.SHIELD, GameConfig.SPECIAL_MAX_USES));
    }

    @Override
    public CharacterClass getCharacterClass() {
        return CharacterClass.KNIGHT;
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
        setShielded(true);
        context.log(getName() + " raises a shield!");
    }
}
