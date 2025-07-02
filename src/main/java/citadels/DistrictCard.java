package citadels;

/**
 * Represents a district card in the Citadels game.
 * Each district card has a name, color, cost, and may have a special ability.
 */
public class DistrictCard {
    /** The name of the district */
    private String name;
    /** The color of the district (yellow, blue, green, red, purple) */
    private String color;
    /** The cost to build the district */
    private int cost;
    /** The special ability text for the district, if any */
    private String specialAbility;

    /**
     * Constructs a new DistrictCard.
     * @param name The name of the district
     * @param color The color of the district
     * @param cost The cost to build the district
     * @param specialAbility The special ability text (may be empty)
     */
    public DistrictCard(String name, String color, int cost, String specialAbility) {
        this.name = name;
        this.color = color;
        this.cost = cost;
        this.specialAbility = specialAbility;
    }

    /**
     * Gets the name of the district.
     * @return The district name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color of the district.
     * @return The district color
     */
    public String getColor() {
        return color;
    }

    /**
     * Gets the cost to build the district.
     * @return The district cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * Gets the special ability text for the district, if any.
     * @return The special ability text
     */
    public String getSpecialAbility() {
        return specialAbility;
    }

    /**
     * Checks if this district card is equal to another object.
     * @param obj The object to compare
     * @return True if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DistrictCard other = (DistrictCard) obj;
        return cost == other.cost && 
               name.equals(other.name) && 
               color.equals(other.color) && 
               specialAbility.equals(other.specialAbility);
    }

    /**
     * Returns the hash code for this district card.
     * @return The hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + color.hashCode();
        result = 31 * result + cost;
        result = 31 * result + specialAbility.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the district card.
     * @return The string representation
     */
    @Override
    public String toString() {
        return name + " [" + color + "] (" + cost + ") - " + specialAbility;
    }
}