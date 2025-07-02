/*
    USYD CODE CITATION ACKNOWLEDGEMENT
    I HEARBY ACKNOWLEDGE THE USE OF AI TOOLS IN THE DEVELOPMENT OF THIS PROJECT.
*/
package citadels;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DistrictCardTest {
    @Test
    void testConstructor() {
        DistrictCard card = new DistrictCard("Noble House", "Noble", 3, "None");
        assertEquals("Noble House", card.getName());
        assertEquals("Noble", card.getColor());
        assertEquals(3, card.getCost());
        assertEquals("None", card.getSpecialAbility());
    }

    @Test
    void testEquals() {
        DistrictCard card1 = new DistrictCard("Tavern", "green", 1, "Gain 1 gold");
        DistrictCard card2 = new DistrictCard("Tavern", "green", 1, "Gain 1 gold");
        DistrictCard card3 = new DistrictCard("Market", "green", 2, "Gain 2 gold");
        
        assertTrue(card1.equals(card2), "Cards with same properties should be equal");
        assertFalse(card1.equals(card3), "Cards with different properties should not be equal");
        assertFalse(card1.equals(null), "Card should not equal null");
        assertTrue(card1.equals(card1), "Card should equal itself");
    }

    @Test
    void testHashCode() {
        DistrictCard card1 = new DistrictCard("Tavern", "green", 1, "Gain 1 gold");
        DistrictCard card2 = new DistrictCard("Tavern", "green", 1, "Gain 1 gold");
        DistrictCard card3 = new DistrictCard("Market", "green", 2, "Gain 2 gold");
        
        assertEquals(card1.hashCode(), card2.hashCode(), "Equal cards should have same hash code");
        assertNotEquals(card1.hashCode(), card3.hashCode(), "Different cards should have different hash codes");
    }

    @Test
    void testToString() {
        DistrictCard card = new DistrictCard("Tavern", "green", 1, "Gain 1 gold");
        String expected = "Tavern [green] (1) - Gain 1 gold";
        assertEquals(expected, card.toString(), "toString should match expected format");
    }
} 