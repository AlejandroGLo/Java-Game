package characters;

import game.BattleContext;
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
    public String getClassLabel() {
        return "Archer";
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
