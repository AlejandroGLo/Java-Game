package attacks;

import characters.Character;
import game.BattleContext;

import java.awt.Graphics2D;

/**
 * One usable action in a character's inventory.
 *
 * Everything the inventory menu displays (name, description, stats, icon) comes
 * from the attack itself, so adding a new attack makes it show up in the menu
 * with no menu changes.
 */
public interface Attack {

    String getName();

    /** One or two sentences describing what the attack does. */
    String getDescription();

    /** Base damage before class multipliers; 0 or less renders as "—". */
    int getBaseDamage();

    AttackRange getRange();

    /** Maximum uses per match, or {@link #UNLIMITED}. */
    int getMaxUses();

    int UNLIMITED = -1;

    /** Draws the attack's icon into a square box. Reused by the menu and the HUD. */
    void drawIcon(Graphics2D g, int x, int y, int size);

    void execute(BattleContext context, Character user, double aimAngleDegrees, double power);
}
