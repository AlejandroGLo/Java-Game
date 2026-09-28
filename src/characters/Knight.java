package characters;

import game.BattleContext;
import moves.Ability;
import moves.Move;

import java.awt.Color;

public class Knight extends Character {
    private static final Move PRIMARY = new Move("Hammer Throw", 28, 30, 11, 1.3);
    private static final Ability SPECIAL = new Ability("Shield Block", "Raises a shield that halves the next hit taken.");

    public Knight(String name, int playerId) {
        super(name, playerId, 130, new Color(150, 150, 170));
    }

    @Override
    public String getClassLabel() {
        return "Knight";
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
