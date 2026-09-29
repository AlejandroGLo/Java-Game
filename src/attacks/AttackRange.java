package attacks;

/** Coarse range band shown in the inventory menu. */
public enum AttackRange {
    MELEE("Melee"),
    SHORT("Short"),
    MEDIUM("Medium"),
    LONG("Long"),
    MAP_WIDE("Map-wide");

    private final String label;

    AttackRange(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
