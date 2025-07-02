/*
    USYD CODE CITATION ACKNOWLEDGEMENT
    I HEARBY ACKNOWLEDGE THE USE OF AI TOOLS IN THE DEVELOPMENT OF THIS PROJECT.
*/
package citadels;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CharacterCardTest {
    @Test
    void testConstructor() {
        CharacterCard card = new CharacterCard("King", 4, "Take crown");
        assertEquals("King", card.getName());
        assertEquals(4, card.getNumber());
        assertEquals("Take crown", card.getSpecialAbility());
    }

    @Test
    void testEquals() {
        CharacterCard card1 = new CharacterCard("King", 4, "Gain gold for yellow districts");
        CharacterCard card2 = new CharacterCard("King", 4, "Gain gold for yellow districts");
        CharacterCard card3 = new CharacterCard("Queen", 5, "Different ability");
        
        assertTrue(card1.equals(card2), "Cards with same properties should be equal");
        assertFalse(card1.equals(card3), "Cards with different properties should not be equal");
        assertFalse(card1.equals(null), "Card should not equal null");
        assertTrue(card1.equals(card1), "Card should equal itself");
    }

    @Test
    void testHashCode() {
        CharacterCard card1 = new CharacterCard("King", 4, "Gain gold for yellow districts");
        CharacterCard card2 = new CharacterCard("King", 4, "Gain gold for yellow districts");
        CharacterCard card3 = new CharacterCard("Queen", 5, "Different ability");
        
        assertEquals(card1.hashCode(), card2.hashCode(), "Equal cards should have same hash code");
        assertNotEquals(card1.hashCode(), card3.hashCode(), "Different cards should have different hash codes");
    }

    @Test
    void testToString() {
        CharacterCard card = new CharacterCard("King", 4, "Gain gold for yellow districts");
        String expected = "King (4) - Gain gold for yellow districts";
        assertEquals(expected, card.toString(), "toString should match expected format");
    }
} 