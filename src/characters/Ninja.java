package characters;

import game.BattleContext;
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
    public String getClassLabel() {
        return "Ninja";
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
