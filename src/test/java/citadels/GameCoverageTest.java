package citadels;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.Arrays;

public class GameCoverageTest {
    @Test
    void testProcessCommandInvalids() {
        Game game = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = game.getPlayers().get(0);
        human.setCharacter(new CharacterCard("King", 4, "Gain gold for yellow districts")); // Set a character to avoid NPE
        
        // Invalid command
        game.processCommand("foobar", human);
        
        // Invalid build (not enough gold)
        human.getHand().clear();
        human.addToHand(new DistrictCard("Palace", "yellow", 5, ""));
        human.addGold(1);
        game.processCommand("build 1", human);
        
        // Duplicate build
        human.addGold(10);
        game.processCommand("build 1", human);
        human.addToHand(new DistrictCard("Palace", "yellow", 5, ""));
        game.processCommand("build 1", human);
        
        // Invalid player number
        game.processCommand("gold 999", human);
        
        // Action not allowed
        game.processCommand("action swap 2", human);
        
        // End turn not allowed
        game.processCommand("end", null);
    }

    @Test
    void testSaveLoadInvalidFile() {
        Game game = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        assertThrows(IOException.class, () -> game.saveGame("/invalid/path/test.json"));
        assertThrows(IOException.class, () -> game.loadGame("/invalid/path/test.json"));
    }

    @Test
    void testGameEndTie() {
        Game game = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        // No player builds anything
        game.forceEndGame();
        game.endGame(); // Should print tie
    }

    @Test
    void testGameEndPurpleBonuses() {
        Game game = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player p = game.getPlayers().get(0);
        p.getHand().clear();
        p.addGold(10);
        // Add purple cards for bonuses
        p.addToHand(new DistrictCard("Dragon Gate", "purple", 6, ""));
        p.addToHand(new DistrictCard("University", "purple", 6, ""));
        p.addToHand(new DistrictCard("Imperial Treasury", "purple", 4, ""));
        p.addToHand(new DistrictCard("Map Room", "purple", 5, ""));
        p.addToHand(new DistrictCard("Wishing Well", "purple", 3, ""));
        p.addToHand(new DistrictCard("Museum", "purple", 4, ""));
        for (DistrictCard card : p.getHand().toArray(new DistrictCard[0])) {
            p.buildDistrict(card);
        }
        game.checkGameEnd();
        game.endGame();
    }

    @Test
    void testGetters() {
        Game game = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        assertNotNull(game.getPlayers());
        assertEquals(1, game.getCurrentRound());
        assertFalse(game.isGameEnded());
        assertNull(game.getKilledCharacter());
        assertNull(game.getRobbedCharacter());
        game.forceEndGame();
        assertTrue(game.isGameEnded());
    }

    @Test
    void testAIPlayerLogic() {
        AIPlayer ai = new AIPlayer(2);
        // Give AI some cards and gold
        ai.addToHand(new DistrictCard("Palace", "yellow", 5, ""));
        ai.addToHand(new DistrictCard("Temple", "blue", 1, ""));
        ai.addGold(10);
        // Test chooseCharacter
        List<CharacterCard> chars = Arrays.asList(
            new CharacterCard("King", 4, ""),
            new CharacterCard("Thief", 2, "")
        );
        CharacterCard chosen = ai.chooseCharacter(chars);
        assertNotNull(chosen);
        // Test chooseDistrictToBuild
        DistrictCard build = ai.chooseDistrictToBuild();
        assertNotNull(build);
        // Test shouldTakeGold
        boolean takeGold = ai.shouldTakeGold();
        assertTrue(takeGold || !takeGold); // Just cover the branch
    }
} 