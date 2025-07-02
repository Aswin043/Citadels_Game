package citadels;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Citadels game.
 * A player can be human or AI, and manages their hand, city, gold, and character.
 */
public class Player {
    /** The player's number (1-based index) */
    private int playerNumber;
    /** True if this player is human, false if AI */
    private boolean isHuman;
    /** The amount of gold the player currently has */
    private int gold;
    /** The list of district cards in the player's hand */
    private List<DistrictCard> hand;
    /** The list of district cards built in the player's city */
    private List<DistrictCard> city;
    /** The character card chosen by the player for the current round */
    private CharacterCard character;
    /** True if this player currently holds the crown */
    private boolean hasCrown;

    /**
     * Constructs a new Player.
     * @param playerNumber The player's number
     * @param isHuman True if the player is human, false if AI
     */
    public Player(int playerNumber, boolean isHuman) {
        this.playerNumber = playerNumber;
        this.isHuman = isHuman;
        this.gold = 0;
        this.hand = new ArrayList<>();
        this.city = new ArrayList<>();
        this.character = null;
        this.hasCrown = false;
    }

    /**
     * Gets the player's number.
     * @return The player's number
     */
    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * Returns true if this player is human.
     * @return True if human, false if AI
     */
    public boolean isHuman() {
        return isHuman;
    }

    /**
     * Gets the amount of gold the player has.
     * @return The player's gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * Adds or removes gold from the player.
     * @param amount The amount to add (can be negative)
     */
    public void addGold(int amount) {
        this.gold += amount;
    }

    /**
     * Gets the player's hand of district cards.
     * @return The list of district cards in hand
     */
    public List<DistrictCard> getHand() {
        return hand;
    }

    /**
     * Adds a district card to the player's hand.
     * @param card The card to add
     */
    public void addToHand(DistrictCard card) {
        hand.add(card);
    }

    /**
     * Removes a district card from the player's hand.
     * @param card The card to remove
     */
    public void discardCard(DistrictCard card) {
        hand.remove(card);
    }

    /**
     * Gets the list of districts built in the player's city.
     * @return The list of built district cards
     */
    public List<DistrictCard> getCity() {
        return city;
    }

    /**
     * Attempts to build a district in the player's city.
     * @param card The district card to build
     * @return True if the district was built, false otherwise
     */
    public boolean buildDistrict(DistrictCard card) {
        if (!hand.contains(card)) {
            return false;
        }
        if (gold < card.getCost()) {
            return false;
        }
        gold -= card.getCost();
        hand.remove(card);
        city.add(card);
        return true;
    }

    /**
     * Gets the character card chosen by the player for the current round.
     * @return The character card
     */
    public CharacterCard getCharacter() {
        return character;
    }

    /**
     * Sets the character card for the player for the current round.
     * @param character The character card
     */
    public void setCharacter(CharacterCard character) {
        this.character = character;
    }

    /**
     * Returns true if this player currently holds the crown.
     * @return True if the player has the crown
     */
    public boolean hasCrown() {
        return hasCrown;
    }

    /**
     * Sets whether this player holds the crown.
     * @param hasCrown True if the player has the crown
     */
    public void setHasCrown(boolean hasCrown) {
        this.hasCrown = hasCrown;
    }

    /**
     * Gets the number of districts built in the player's city.
     * @return The city size
     */
    public int getCitySize() {
        return city.size();
    }

    /**
     * Calculates the player's score based on built districts and bonuses.
     * @return The player's score
     */
    public int calculateScore() {
        int score = 0;
        
        // Base score from districts
        for (DistrictCard card : city) {
            score += card.getCost();
        }
        
        // First to 8 districts bonus
        if (city.size() >= 8) {
            score += 2;
        }
        
        // Color bonus (2 points for having all 5 colors)
        boolean hasNoble = false;
        boolean hasReligious = false;
        boolean hasTrade = false;
        boolean hasMilitary = false;
        boolean hasSpecial = false;
        
        for (DistrictCard card : city) {
            String color = card.getColor().toLowerCase();
            switch (color) {
                case "yellow":
                case "noble":
                    hasNoble = true;
                    break;
                case "blue":
                case "religious":
                    hasReligious = true;
                    break;
                case "green":
                case "trade":
                    hasTrade = true;
                    break;
                case "red":
                case "military":
                    hasMilitary = true;
                    break;
                case "purple":
                case "special":
                    hasSpecial = true;
                    break;
            }
        }
        
        // Count unique colors
        int uniqueColors = 0;
        if (hasNoble) uniqueColors++;
        if (hasReligious) uniqueColors++;
        if (hasTrade) uniqueColors++;
        if (hasMilitary) uniqueColors++;
        if (hasSpecial) uniqueColors++;
        
        // Add bonus for having all 5 colors
        if (uniqueColors == 5) {
            score += 2;
        }
        
        return score;
    }
}