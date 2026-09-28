package game;

import characters.Character;
import moves.Move;

import java.util.List;

public interface BattleContext {
    List<Character> getAllCharacters();

    double getWorldWidth();

    double getGroundY();

    void spawnProjectile(Character owner, Move move, double angleDegrees, double power, double angleOffsetDegrees);

    void log(String message);
}
