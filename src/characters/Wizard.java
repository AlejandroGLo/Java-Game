package characters;

import game.BattleContext;
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

    @Override
    public String getClassLabel() {
        return "Wizard";
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
        newX = Math.max(0, Math.min(context.getWorldWidth() - WIDTH, newX));
        setPosition(newX, getY());
        setVy(0);
        context.log(getName() + " blinks away!");
    }
}
