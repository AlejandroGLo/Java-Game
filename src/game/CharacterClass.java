package game;

import characters.Archer;
import characters.Character;
import characters.Knight;
import characters.Ninja;
import characters.Wizard;

public enum CharacterClass {
    KNIGHT("Knight"),
    WIZARD("Wizard"),
    ARCHER("Archer"),
    NINJA("Ninja");

    private final String label;

    CharacterClass(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Character create(String name, int playerId) {
        switch (this) {
            case KNIGHT:
                return new Knight(name, playerId);
            case WIZARD:
                return new Wizard(name, playerId);
            case ARCHER:
                return new Archer(name, playerId);
            case NINJA:
                return new Ninja(name, playerId);
            default:
                throw new IllegalStateException("Unknown class: " + this);
        }
    }
}
