package attacks;

/**
 * A character's three action slots: two attacks and a special.
 *
 * Slot 1 is re-selected at the start of every turn, and limited-use attacks
 * (specials) track their remaining uses here rather than on the attack itself,
 * since attack definitions are shared between characters.
 */
public class Inventory {

    public static final int SLOT_ATTACK_1 = 0;
    public static final int SLOT_ATTACK_2 = 1;
    public static final int SLOT_SPECIAL = 2;
    public static final int SLOTS = 3;

    private final Attack[] slots = new Attack[SLOTS];
    private final int[] usesLeft = new int[SLOTS];
    private int selected = SLOT_ATTACK_1;

    public Inventory(Attack attack1, Attack attack2, Attack special) {
        slots[SLOT_ATTACK_1] = attack1;
        slots[SLOT_ATTACK_2] = attack2;
        slots[SLOT_SPECIAL] = special;
        for (int i = 0; i < SLOTS; i++) {
            usesLeft[i] = slots[i] == null ? 0 : slots[i].getMaxUses();
        }
    }

    public Attack get(int slot) {
        return slots[slot];
    }

    public Attack getSelected() {
        return slots[selected];
    }

    public int getSelectedSlot() {
        return selected;
    }

    /** @return true if the slot was selectable and is now active. */
    public boolean select(int slot) {
        if (slot < 0 || slot >= SLOTS || !isUsable(slot)) return false;
        selected = slot;
        return true;
    }

    public boolean isUsable(int slot) {
        if (slot < 0 || slot >= SLOTS || slots[slot] == null) return false;
        return usesLeft[slot] != 0;
    }

    /** @return remaining uses, or {@link Attack#UNLIMITED}. */
    public int getUsesLeft(int slot) {
        return usesLeft[slot];
    }

    public boolean isLimited(int slot) {
        return slots[slot] != null && slots[slot].getMaxUses() != Attack.UNLIMITED;
    }

    public void consume(int slot) {
        if (isLimited(slot) && usesLeft[slot] > 0) {
            usesLeft[slot]--;
            if (usesLeft[slot] == 0 && selected == slot) {
                selected = SLOT_ATTACK_1;
            }
        }
    }

    /** Attack 1 is the default action each turn. */
    public void resetForTurn() {
        selected = SLOT_ATTACK_1;
    }
}
