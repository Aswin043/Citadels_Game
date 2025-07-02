/*
    USYD CODE CITATION ACKNOWLEDGEMENT
    I HEARBY ACKNOWLEDGE THE USE OF AI TOOLS IN THE DEVELOPMENT OF THIS PROJECT.
*/
package citadels;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;

public class PlayerTest {
    private Player player;
    private DistrictCard nobleCard;
    private DistrictCard religiousCard;
    private DistrictCard tradeCard;
    private DistrictCard militaryCard;
    private DistrictCard specialCard;

    @BeforeEach
    public void setUp() {
        player = new Player(1, true);
        nobleCard = new DistrictCard("Noble House", "Noble", 3, "None");
        religiousCard = new DistrictCard("Temple", "Religious", 2, "None");
        tradeCard = new DistrictCard("Market", "Trade", 2, "None");
        militaryCard = new DistrictCard("Barracks", "Military", 3, "None");
        specialCard = new DistrictCard("Dragon Gate", "Special", 6, "None");
    }

    @Test
    void testInitialSetup() {
        assertEquals(1, player.getPlayerNumber());
        assertTrue(player.isHuman());
        assertEquals(0, player.getGold());
        assertTrue(player.getHand().isEmpty());
        assertTrue(player.getCity().isEmpty());
        assertNull(player.getCharacter());
        assertFalse(player.hasCrown());
    }

    @Test
    void testAddGold() {
        player.addGold(5);
        assertEquals(5, player.getGold());
        player.addGold(-2);
        assertEquals(3, player.getGold());
    }

    @Test
    void testAddToHand() {
        DistrictCard card = new DistrictCard("Tavern", "green", 1, "");
        player.addToHand(card);
        assertEquals(1, player.getHand().size());
        assertTrue(player.getHand().contains(card));
    }

    @Test
    void testBuildDistrict() {
        DistrictCard card = new DistrictCard("Tavern", "green", 1, "");
        player.addToHand(card);
        player.addGold(1);
        
        assertTrue(player.buildDistrict(card));
        assertEquals(1, player.getCity().size());
        assertEquals(0, player.getGold());
        assertTrue(player.getHand().isEmpty());
    }

    @Test
    void testBuildDistrictNotInHand() {
        DistrictCard card = new DistrictCard("Tavern", "green", 1, "");
        player.addGold(1);
        
        assertFalse(player.buildDistrict(card));
        assertEquals(0, player.getCity().size());
        assertEquals(1, player.getGold());
    }

    @Test
    void testBuildDistrictNotEnoughGold() {
        DistrictCard card = new DistrictCard("Palace", "yellow", 5, "");
        player.addToHand(card);
        player.addGold(3);
        
        assertFalse(player.buildDistrict(card));
        assertEquals(0, player.getCity().size());
        assertEquals(3, player.getGold());
        assertEquals(1, player.getHand().size());
    }

    @Test
    void testDiscardCard() {
        player.addToHand(nobleCard);
        player.addToHand(religiousCard);
        player.discardCard(nobleCard);
        assertEquals(1, player.getHand().size());
        assertFalse(player.getHand().contains(nobleCard));
        assertTrue(player.getHand().contains(religiousCard));
    }

    @Test
    void testSetCharacter() {
        CharacterCard character = new CharacterCard("King", 4, "Gain gold for yellow districts");
        player.setCharacter(character);
        assertEquals(character, player.getCharacter());
    }

    @Test
    void testSetHasCrown() {
        player.setHasCrown(true);
        assertTrue(player.hasCrown());
        player.setHasCrown(false);
        assertFalse(player.hasCrown());
    }

    @Test
    void testCalculateScore() {
        // Add some districts of different colors
        DistrictCard card1 = new DistrictCard("Tavern", "green", 1, "");
        DistrictCard card2 = new DistrictCard("Market", "green", 2, "");
        DistrictCard card3 = new DistrictCard("Palace", "yellow", 5, "");
        
        player.addToHand(card1);
        player.addToHand(card2);
        player.addToHand(card3);
        player.addGold(8);
        
        player.buildDistrict(card1);
        player.buildDistrict(card2);
        player.buildDistrict(card3);
        
        int score = player.calculateScore();
        assertEquals(8, score); // 1 + 2 + 5 = 8
    }

    @Test
    void testCalculateScoreWithFirstToEight() {
        // Build 8 districts
        for (int i = 0; i < 8; i++) {
            DistrictCard card = new DistrictCard("District " + i, "blue", 1, "");
            player.addToHand(card);
            player.addGold(1);
            player.buildDistrict(card);
        }
        
        int score = player.calculateScore();
        assertEquals(10, score); // 8 points for districts + 2 points for first to 8
    }

    @Test
    void testCalculateScoreWithColorBonus() {
        // Add one district of each color
        player.addToHand(nobleCard);
        player.addToHand(religiousCard);
        player.addToHand(tradeCard);
        player.addToHand(militaryCard);
        player.addToHand(specialCard);
        
        player.addGold(16); // Enough gold to build all districts
        
        player.buildDistrict(nobleCard);
        player.buildDistrict(religiousCard);
        player.buildDistrict(tradeCard);
        player.buildDistrict(militaryCard);
        player.buildDistrict(specialCard);
        
        int score = player.calculateScore();
        assertEquals(18, score); // 16 points for districts (3+2+2+3+6) + 2 points for having all 5 colors
    }
} 