package game;

import attacks.MeleeAttack;
import characters.Character;
import moves.Move;

import java.util.List;

public interface BattleContext {
    List<Character> getAllCharacters();

    double getWorldWidth();

    double getGroundY();

    void spawnProjectile(Character owner, Move move, double angleDegrees, double power, double angleOffsetDegrees);

    /** Begins a weapon swing in front of the character; the battle screen simulates it. */
    void startMeleeSwing(Character user, MeleeAttack attack);

    void log(String message);
}
