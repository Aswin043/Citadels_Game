package citadels;

/**
 * Represents a character card in the Citadels game.
 * Each character has a name, a unique number, and a special ability.
 */
public class CharacterCard {
    /** The name of the character */
    private String name;
    /** The unique number of the character (1-8) */
    private int number;
    /** The special ability text for the character */
    private String specialAbility;

    /**
     * Constructs a new CharacterCard.
     * @param name The name of the character
     * @param number The unique number of the character
     * @param specialAbility The special ability text
     */
    public CharacterCard(String name, int number, String specialAbility) {
        this.name = name;
        this.number = number;
        this.specialAbility = specialAbility;
    }

    /**
     * Gets the name of the character.
     * @return The character name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unique number of the character.
     * @return The character number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the special ability text for the character.
     * @return The special ability text
     */
    public String getSpecialAbility() {
        return specialAbility;
    }

    /**
     * Checks if this character card is equal to another object.
     * @param obj The object to compare
     * @return True if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CharacterCard other = (CharacterCard) obj;
        return number == other.number && 
               name.equals(other.name) && 
               specialAbility.equals(other.specialAbility);
    }

    /**
     * Returns the hash code for this character card.
     * @return The hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + number;
        result = 31 * result + specialAbility.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the character card.
     * @return The string representation
     */
    @Override
    public String toString() {
        return name + " (" + number + ") - " + specialAbility;
    }
}