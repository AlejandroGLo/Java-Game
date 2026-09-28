package game;

import characters.Character;
import moves.Move;

class Projectile {
    double x, y, vx, vy;
    /** Distance flown so far, used for the range limit and damage falloff. */
    double travelled;
    final Move move;
    final Character owner;

    Projectile(double x, double y, double vx, double vy, Move move, Character owner) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.move = move;
        this.owner = owner;
    }
}
