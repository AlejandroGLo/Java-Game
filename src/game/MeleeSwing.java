package game;

import attacks.MeleeAttack;
import characters.Character;

import java.util.HashSet;
import java.util.Set;

/**
 * A swing in progress.
 *
 * The damaging region is the half-disc in front of the character: every point
 * within the weapon's reach of the shoulder pivot, on the side the character is
 * facing. It runs from straight overhead, through straight ahead, to straight
 * down. Targets are only checked once the wind-up has finished, and each target
 * can be hit at most once per swing.
 */
class MeleeSwing {

    final Character owner;
    final MeleeAttack attack;
    final boolean facingRight;
    final Set<Character> alreadyHit = new HashSet<>();

    double elapsed;

    MeleeSwing(Character owner, MeleeAttack attack) {
        this.owner = owner;
        this.attack = attack;
        this.facingRight = owner.isFacingRight();
    }

    double pivotX() {
        double dir = facingRight ? 1 : -1;
        return owner.getX() + owner.getWidth() / 2.0 + dir * (owner.getWidth() * 0.18);
    }

    double pivotY() {
        return owner.getY() + owner.getHeight() * 0.30;
    }

    double totalSeconds() {
        return attack.getWindupSeconds() + attack.getSwingSeconds();
    }

    boolean isFinished() {
        return elapsed >= totalSeconds();
    }

    /** True once the weapon is actually travelling through the arc. */
    boolean isActive() {
        return elapsed >= attack.getWindupSeconds() && !isFinished();
    }

    /** 0 at the top of the arc, 1 at the bottom. */
    double sweepProgress() {
        if (elapsed <= attack.getWindupSeconds()) return 0;
        return Math.min(1, (elapsed - attack.getWindupSeconds()) / attack.getSwingSeconds());
    }

    /**
     * Whether a character's body overlaps the half-disc.
     *
     * The body box is first clipped to the facing side of the pivot, then the
     * closest remaining point is compared against the weapon's reach, which
     * makes this an exact test rather than a centre-point approximation.
     */
    boolean covers(Character target) {
        double px = pivotX(), py = pivotY();
        double minX = target.getX();
        double maxX = target.getX() + target.getWidth();
        double minY = target.getY();
        double maxY = target.getY() + target.getHeight();

        // Clip to the half-plane the character is facing.
        if (facingRight) {
            minX = Math.max(minX, px);
        } else {
            maxX = Math.min(maxX, px);
        }
        if (minX > maxX) return false;

        double closestX = Math.max(minX, Math.min(px, maxX));
        double closestY = Math.max(minY, Math.min(py, maxY));
        double reach = attack.getRadius();
        return Math.hypot(closestX - px, closestY - py) <= reach;
    }
}
