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

public class Wizard extends Character {
    private static final Move PRIMARY = new Move("Fireball", 24, 55, 10, 1.0);
    private static final Ability SPECIAL = new Ability("Blink", "Teleports a short distance in the direction you're facing.");
    private static final double BLINK_DISTANCE = 120;

    public Wizard(String name, int playerId) {
        super(name, playerId, 95, new Color(130, 80, 210));
    }


    private static final Move BOLT = new Move("Arcane Bolt", 15, 14, 14, 0.75);

    @Override
    protected Inventory createInventory() {
        return new Inventory(
                new ProjectileAttack("Fireball",
                        "A slow orb of flame that bursts on impact, damaging everything nearby.",
                        PRIMARY, AttackRange.MEDIUM, AttackIcons.Kind.FIREBALL),
                new ProjectileAttack("Arcane Bolt",
                        "A fast, flat bolt of energy. Less damage than the fireball but far easier to aim.",
                        BOLT, AttackRange.LONG, AttackIcons.Kind.BOLT),
                new SpecialAttack("Blink",
                        "Teleports the wizard a short distance in the direction it is facing.",
                        0, AttackRange.SHORT, AttackIcons.Kind.TELEPORT, GameConfig.SPECIAL_MAX_USES));
    }

    @Override
    public CharacterClass getCharacterClass() {
        return CharacterClass.WIZARD;
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
        double newX = getX() + dir * BLINK_DISTANCE;
        newX = Math.max(0, Math.min(context.getWorldWidth() - getWidth(), newX));
        setPosition(newX, getY());
        setVy(0);
        context.log(getName() + " blinks away!");
    }
}
