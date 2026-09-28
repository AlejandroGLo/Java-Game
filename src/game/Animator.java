package game;

import characters.Character;

/**
 * Per-character animation state machine. The state is derived from what the
 * character is actually doing (velocity, grounded) rather than from which key
 * is held, so knockback, dashes and blinks animate correctly too.
 *
 * Produces a pose that CharacterSprite draws: a rotation per limb, plus body
 * bob, squash/stretch and a hit flash.
 */
class Animator {

    private AnimState state = AnimState.IDLE;
    private double stateTime;
    private double runPhase;

    private double attackTimer;
    private double hitTimer;
    private double squashTimer;
    private double stretchTimer;
    private double aimDegrees;

    private boolean wasOnGround = true;
    private double lastVy;
    private boolean landed;
    private boolean tookOff;
    private int lastFacing;
    private boolean turned;

    // ---- Pose output (read by the renderer) ----
    double backArmDeg, frontArmDeg, backLegDeg, frontLegDeg;
    double bodyOffsetY;
    double scaleX = 1, scaleY = 1;
    double leanDeg;
    double flashAlpha;

    /** True exactly once, on the frame the character touched down hard enough to kick up dust. */
    boolean consumeLanded() {
        boolean v = landed;
        landed = false;
        return v;
    }

    boolean consumeTookOff() {
        boolean v = tookOff;
        tookOff = false;
        return v;
    }

    boolean consumeTurned() {
        boolean v = turned;
        turned = false;
        return v;
    }

    void triggerAttack(double aimDegrees) {
        this.aimDegrees = aimDegrees;
        attackTimer = GameConfig.ATTACK_ANIM_SECONDS;
    }

    void triggerHit() {
        hitTimer = GameConfig.HIT_ANIM_SECONDS;
    }

    void update(Character c, double dt) {
        double vx = c.getVx();
        double vy = c.getVy();
        boolean onGround = c.isOnGround();

        if (attackTimer > 0) attackTimer -= dt;
        if (hitTimer > 0) hitTimer -= dt;
        if (squashTimer > 0) squashTimer -= dt;
        if (stretchTimer > 0) stretchTimer -= dt;

        // Landing / takeoff detection drives squash, stretch and dust.
        if (onGround && !wasOnGround) {
            if (lastVy >= GameConfig.SQUASH_MIN_LANDING_SPEED) {
                squashTimer = GameConfig.SQUASH_SECONDS;
                landed = true;
            }
        } else if (!onGround && wasOnGround && vy < 0) {
            stretchTimer = GameConfig.STRETCH_SECONDS;
            tookOff = true;
        }
        wasOnGround = onGround;
        lastVy = vy;

        int facing = c.isFacingRight() ? 1 : -1;
        if (lastFacing != 0 && facing != lastFacing && onGround && Math.abs(vx) > 20) {
            turned = true;
        }
        lastFacing = facing;

        AnimState next;
        if (hitTimer > 0) {
            next = AnimState.HIT;
        } else if (attackTimer > 0) {
            next = AnimState.ATTACK;
        } else if (!onGround) {
            next = vy < 0 ? AnimState.JUMP : AnimState.FALL;
        } else if (Math.abs(vx) > 12) {
            next = AnimState.RUN;
        } else {
            next = AnimState.IDLE;
        }

        if (next != state) {
            state = next;
            stateTime = 0;
        }
        stateTime += dt;

        pose(vx, dt);
    }

    private void pose(double vx, double dt) {
        double speed = Math.abs(vx);

        // Legs keep cycling whenever the character is actually moving along the
        // ground, even mid-attack, so a shot fired while walking still looks right.
        if (state == AnimState.RUN || (speed > 12 && (state == AnimState.ATTACK || state == AnimState.HIT))) {
            runPhase += speed * GameConfig.RUN_CYCLE_RATE * dt;
        } else if (state != AnimState.RUN) {
            runPhase *= Math.max(0, 1 - 8 * dt); // settle back to neutral
        }

        double swing = Math.sin(runPhase);
        backArmDeg = frontArmDeg = backLegDeg = frontLegDeg = 0;
        bodyOffsetY = 0;
        leanDeg = 0;

        switch (state) {
            case RUN: {
                backLegDeg = swing * GameConfig.RUN_LEG_SWING_DEG;
                frontLegDeg = -swing * GameConfig.RUN_LEG_SWING_DEG;
                // Opposite arm swings with each leg.
                frontArmDeg = swing * GameConfig.RUN_ARM_SWING_DEG;
                backArmDeg = -swing * GameConfig.RUN_ARM_SWING_DEG;
                bodyOffsetY = -Math.abs(Math.sin(runPhase * 2)) * GameConfig.RUN_BOB_PIXELS;
                break;
            }
            case IDLE: {
                double breath = Math.sin(stateTime * GameConfig.IDLE_BREATH_SPEED);
                bodyOffsetY = breath * GameConfig.IDLE_BREATH_PIXELS;
                frontArmDeg = breath * GameConfig.IDLE_ARM_SWAY_DEG;
                backArmDeg = -breath * GameConfig.IDLE_ARM_SWAY_DEG;
                break;
            }
            case JUMP: {
                backLegDeg = -GameConfig.JUMP_TUCK_DEG;
                frontLegDeg = -GameConfig.JUMP_TUCK_DEG * 0.6;
                frontArmDeg = GameConfig.JUMP_ARM_FRONT_DEG;
                backArmDeg = GameConfig.JUMP_ARM_BACK_DEG;
                break;
            }
            case FALL: {
                backLegDeg = GameConfig.FALL_SPLAY_DEG;
                frontLegDeg = -GameConfig.FALL_SPLAY_DEG * 0.5;
                frontArmDeg = GameConfig.FALL_ARM_FRONT_DEG;
                backArmDeg = GameConfig.FALL_ARM_BACK_DEG;
                break;
            }
            case ATTACK: {
                double t = 1 - Math.max(0, attackTimer) / GameConfig.ATTACK_ANIM_SECONDS;
                // Punch out fast, settle back slowly.
                double reach = t < 0.25 ? t / 0.25 : 1 - (t - 0.25) / 0.75 * 0.35;
                frontArmDeg = -(90 + aimDegrees) * reach;
                backArmDeg = GameConfig.ATTACK_BACK_ARM_DEG * reach;
                if (speed <= 12) {
                    backLegDeg = -6 * reach;
                    frontLegDeg = 8 * reach;
                } else {
                    backLegDeg = swing * GameConfig.RUN_LEG_SWING_DEG;
                    frontLegDeg = -swing * GameConfig.RUN_LEG_SWING_DEG;
                }
                break;
            }
            case HIT: {
                double t = Math.max(0, hitTimer) / GameConfig.HIT_ANIM_SECONDS;
                leanDeg = GameConfig.HIT_LEAN_DEG * t;
                backArmDeg = GameConfig.HIT_ARM_BACK_DEG * t;
                frontArmDeg = GameConfig.HIT_ARM_FRONT_DEG * t;
                backLegDeg = 10 * t;
                frontLegDeg = -14 * t;
                break;
            }
        }

        // Squash on landing, stretch on takeoff.
        scaleX = 1;
        scaleY = 1;
        if (squashTimer > 0) {
            double k = squashTimer / GameConfig.SQUASH_SECONDS;
            scaleY = 1 - GameConfig.SQUASH_AMOUNT * k;
            scaleX = 1 + GameConfig.SQUASH_AMOUNT * k;
        } else if (stretchTimer > 0) {
            double k = stretchTimer / GameConfig.STRETCH_SECONDS;
            scaleY = 1 + GameConfig.STRETCH_AMOUNT * k;
            scaleX = 1 - GameConfig.STRETCH_AMOUNT * k;
        }

        flashAlpha = hitTimer > 0
                ? Math.min(1, hitTimer / GameConfig.HIT_FLASH_SECONDS)
                : 0;
    }
}
