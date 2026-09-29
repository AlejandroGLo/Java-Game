package game;

/**
 * Every tunable value in the game, in one place.
 *
 * UNITS: all speeds are per SECOND (not per frame) and all durations are in
 * seconds, so the game plays identically at any framerate. Distances are in
 * screen pixels, angles in degrees unless a name says RAD.
 */
public final class GameConfig {

    private GameConfig() {
    }

    // ---- Loop ----------------------------------------------------------

    /** Physics runs in fixed sub-steps of this length, however fast the screen refreshes. */
    public static final double PHYSICS_STEP = 1.0 / 120.0;
    /** Largest frame gap we simulate; anything bigger (window dragged, laptop slept) is dropped. */
    public static final double MAX_FRAME_TIME = 0.25;
    /** Screen refresh target, in milliseconds per repaint. */
    public static final int FRAME_DELAY_MS = 16;

    // ---- World ---------------------------------------------------------

    public static final double GRAVITY = 1620.0;        // px/s^2
    public static final int GROUND_MARGIN = 80;         // ground line, px up from bottom
    public static final double STEP_HEIGHT = 14.0;      // ledge height a character walks up

    // ---- Character movement --------------------------------------------

    public static final double MOVE_SPEED = 240.0;      // px/s top walking speed
    public static final double JUMP_VELOCITY = -630.0;  // px/s upward launch
    /** How fast walking speed is reached / shed. Higher = snappier, lower = more slide. */
    public static final double GROUND_ACCEL = 2400.0;   // px/s^2
    public static final double GROUND_FRICTION = 2000.0;// px/s^2
    public static final double AIR_ACCEL = 900.0;       // px/s^2 (weaker control mid-air)

    // ---- Turn flow ------------------------------------------------------

    public static final double TURN_SECONDS = 20.0;
    public static final double POST_ACTION_SECONDS = 0.75;
    public static final double LOG_SECONDS = 2.0;

    // ---- Aiming and firing ----------------------------------------------

    public static final double MAX_POWER = 100.0;
    public static final double MIN_FIRE_POWER = 18.0;
    public static final double CHARGE_RATE = 96.0;      // power/s while holding fire
    public static final double AIM_RATE = 78.0;         // degrees/s
    public static final double MIN_ANGLE = -80.0;
    public static final double MAX_ANGLE = 80.0;

    /** Move.getProjectileSpeed() is authored per-class in old per-frame units; this bridges to px/s. */
    public static final double PROJECTILE_SPEED_SCALE = 60.0;

    // ---- Hits and damage -------------------------------------------------

    public static final double HIT_RADIUS = 20.0;       // projectile vs character
    public static final double EXPLOSION_FX_SECONDS = 0.33;

    // ---- PART 2: ranged combat ------------------------------------------

    /** Projectiles die after travelling this fraction of the map width. */
    public static final double MAX_RANGE_FRACTION = 0.45;

    /** Random angle jitter per shot, so long shots are less reliable. */
    public static final boolean SPREAD_ENABLED = true;
    public static final double SPREAD_DEGREES = 3.0;

    /** Damage tapers off past FALLOFF_START of max range, down to FALLOFF_MIN at max range. */
    public static final boolean FALLOFF_ENABLED = true;
    public static final double FALLOFF_START_FRACTION = 0.5;
    public static final double FALLOFF_MIN_DAMAGE = 0.55;

    /** Splash also damages teammates and the shooter (Worms-style). */
    public static final boolean FRIENDLY_FIRE = true;

    public static final double KNOCKBACK_SPEED = 150.0; // px/s pushed along the shot direction
    public static final double RECOIL_PIXELS = 5.0;     // shooter kicks back this far
    public static final double MUZZLE_FLASH_SECONDS = 0.12;

    // ---- PART 1: animation ----------------------------------------------

    public static final double ATTACK_ANIM_SECONDS = 0.35;
    public static final double HIT_ANIM_SECONDS = 0.35;

    /** Run cycle: radians of cycle per second, per px/s of speed. */
    public static final double RUN_CYCLE_RATE = 0.055;
    public static final double RUN_LEG_SWING_DEG = 34.0;
    public static final double RUN_ARM_SWING_DEG = 22.0;
    public static final double RUN_BOB_PIXELS = 1.2;

    public static final double IDLE_BREATH_SPEED = 2.2;  // radians/s
    public static final double IDLE_BREATH_PIXELS = 0.9;
    public static final double IDLE_ARM_SWAY_DEG = 3.0;

    public static final double JUMP_TUCK_DEG = 28.0;     // legs pulled up while rising
    public static final double FALL_SPLAY_DEG = 20.0;    // legs reach down while falling

    // Arm poses. Kept modest so the cut-out limbs don't visibly leave their sockets.
    public static final double JUMP_ARM_FRONT_DEG = -18.0;
    public static final double JUMP_ARM_BACK_DEG = 12.0;
    public static final double FALL_ARM_FRONT_DEG = -28.0;
    public static final double FALL_ARM_BACK_DEG = 20.0;
    public static final double ATTACK_BACK_ARM_DEG = 12.0;
    public static final double HIT_ARM_BACK_DEG = -24.0;
    public static final double HIT_ARM_FRONT_DEG = -16.0;

    /** Landing squash / takeoff stretch, as a fraction of height. */
    public static final double SQUASH_AMOUNT = 0.18;
    public static final double SQUASH_SECONDS = 0.18;
    public static final double STRETCH_AMOUNT = 0.12;
    public static final double STRETCH_SECONDS = 0.14;
    /** Downward speed needed before a landing squashes at all. */
    public static final double SQUASH_MIN_LANDING_SPEED = 180.0;

    public static final double HIT_FLASH_SECONDS = 0.30;
    public static final double HIT_LEAN_DEG = 14.0;

    // ---- Dust particles --------------------------------------------------

    public static final boolean DUST_ENABLED = true;
    public static final double DUST_LIFETIME = 0.4;
    public static final int DUST_ON_LANDING = 6;
    public static final int DUST_ON_TURN = 3;
    public static final double DUST_SPEED = 70.0;

    // ---- Character select screen -----------------------------------------

    /** Idle "breathing" loop on each portrait. */
    public static final double SELECT_BREATH_SPEED = 2.4;   // radians/s
    public static final double SELECT_BREATH_SCALE = 0.035; // +/- 3.5% vertical squash
    public static final double SELECT_BREATH_BOB = 1.6;     // grid units of bob
    /** Each slot is offset along the wave so the six portraits don't breathe in unison. */
    public static final double SELECT_BREATH_SLOT_OFFSET = 0.9;

    /** Pop-in played when a slot's character changes. */
    public static final double SELECT_POP_SECONDS = 0.28;
    public static final double SELECT_POP_START_SCALE = 0.8;
    public static final double SELECT_POP_OVERSHOOT = 1.9;

    // ---- Character body sizes --------------------------------------------

    /** Collision box for a normal character. */
    public static final int BODY_WIDTH = 30;
    public static final int BODY_HEIGHT = 46;
    /** Tanks (Orc) are visibly bulkier and occupy a larger box. */
    public static final int TANK_BODY_WIDTH = 38;
    public static final int TANK_BODY_HEIGHT = 56;

    /** Drawn in place of a sprite that failed to build, so nothing is ever invisible. */
    public static final boolean SPRITE_PLACEHOLDER_ENABLED = true;

    // ---- Inventory / attack menu -----------------------------------------

    /** Uses per match for a character's special. */
    public static final int SPECIAL_MAX_USES = 1;
    /** Freeze the turn clock while the inventory menu is open. */
    public static final boolean INVENTORY_PAUSES_TURN_TIMER = false;

    public static final int MENU_SLOT_WIDTH = 250;
    public static final int MENU_SLOT_HEIGHT = 250;
    public static final int MENU_SLOT_GAP = 22;
    public static final int MENU_ICON_SIZE = 62;
}
