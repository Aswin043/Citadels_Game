package citadels;

import java.util.List;

/**
 * Represents an AI player in the Citadels game.
 * Extends the Player class and adds AI-specific decision making.
 */
public class AIPlayer extends Player {
    
    /**
     * Creates a new AI player.
     *
     * @param playerNumber The player's number
     */
    public AIPlayer(int playerNumber) {
        super(playerNumber, false);
    }

    /**
     * Makes a decision about which character to choose.
     * Basic implementation chooses randomly.
     *
     * @param availableCharacters List of available character cards
     * @return The chosen character card
     */
    public CharacterCard chooseCharacter(List<CharacterCard> availableCharacters) {
        // Basic AI: Choose randomly
        int choice = (int) (Math.random() * availableCharacters.size());
        return availableCharacters.get(choice);
    }

    /**
     * Makes a decision about which district to build.
     * Basic implementation builds the most expensive district it can afford.
     *
     * @return The district card to build, or null if no valid choice
     */
    public DistrictCard chooseDistrictToBuild() {
        DistrictCard bestCard = null;
        int maxCost = -1;

        for (DistrictCard card : getHand()) {
            if (card.getCost() <= getGold() && card.getCost() > maxCost) {
                bestCard = card;
                maxCost = card.getCost();
            }
        }

        return bestCard;
    }

    /**
     * Makes a decision about whether to take gold or draw cards.
     * Basic implementation takes gold if it can build something, otherwise draws cards.
     *
     * @return true if the AI should take gold, false to draw cards
     */
    public boolean shouldTakeGold() {
        // Check if we can build any districts
        for (DistrictCard card : getHand()) {
            if (card.getCost() <= getGold()) {
                return true;
            }
        }
        return false;
    }
} 