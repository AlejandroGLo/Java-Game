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

public class Ninja extends Character {
    private static final Move PRIMARY = new Move("Throwing Star", 14, 8, 16, 0.6);
    private static final Ability SPECIAL = new Ability("Shadow Dash", "Dashes forward quickly to reposition out of danger.");
    private static final double DASH_SPEED = 14;

    public Ninja(String name, int playerId) {
        super(name, playerId, 80, new Color(70, 70, 75));
    }


    @Override
    protected Inventory createInventory() {
        return new Inventory(
                new ProjectileAttack("Shuriken",
                        "A single spinning star. Quick and accurate, but light on damage.",
                        PRIMARY, AttackRange.MEDIUM, AttackIcons.Kind.SHURIKEN),
                new ProjectileAttack("Shuriken Volley",
                        "Throws five stars in a fan. Devastating up close, unreliable at range.",
                        PRIMARY, AttackRange.SHORT, AttackIcons.Kind.SHURIKEN, 5, 34),
                new SpecialAttack("Shadow Dash",
                        "Dashes forward in a blur to break away from danger.",
                        0, AttackRange.SHORT, AttackIcons.Kind.TELEPORT, GameConfig.SPECIAL_MAX_USES));
    }

    @Override
    public CharacterClass getCharacterClass() {
        return CharacterClass.NINJA;
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
        double dir = isFacingRight() ? 1 : -1;
        setVx(dir * DASH_SPEED);
        setVy(-4);
        setOnGround(false);
        context.log(getName() + " vanishes in a shadow dash!");
    }
}
