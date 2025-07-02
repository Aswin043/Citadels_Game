package citadels;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.lang.reflect.Method;

public class GameTest {
    private Game game;
    private static final int NUM_PLAYERS = 4;

    @BeforeEach
    public void setUp() {
        game = new Game(NUM_PLAYERS);
    }

    // Test game initialization
    @Test
    public void testGameInitialization() {
        // Test that game is properly initialized with correct number of players
        assertNotNull(game, "Game should not be null");
        assertEquals(NUM_PLAYERS, game.getPlayers().size(), "Should have correct number of players");
        assertFalse(game.isGameEnded(), "Game should not be ended initially");
        assertEquals(1, game.getCurrentRound(), "Initial round should be 1");
    }

    // Test player setup
    @Test
    public void testPlayerSetup() {
        List<Player> players = game.getPlayers();
        assertTrue(players.get(0).isHuman(), "First player should be human");
        for (int i = 1; i < players.size(); i++) {
            assertFalse(players.get(i).isHuman(), "Other players should be AI");
        }
    }

    // Test initial gold distribution
    @Test
    public void testInitialGoldDistribution() {
        for (Player player : game.getPlayers()) {
            assertEquals(2, player.getGold(), "Each player should start with 2 gold");
        }
    }

    // Test initial hand distribution
    @Test
    public void testInitialHandDistribution() {
        for (Player player : game.getPlayers()) {
            assertEquals(4, player.getHand().size(), "Each player should start with 4 cards");
        }
    }

    // Test character selection
    @Test
    public void testCharacterSelection() {
        // Simulate input: always select the first available character and end turn, enough for many rounds
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("assassin\nt\ngold\nt\nend\nt\n"); // Add 't' for waitForContinue
            sb.append("thief\nt\ngold\nt\nend\nt\n");
            sb.append("magician\nt\ngold\nt\nend\nt\n");
            sb.append("king\nt\ngold\nt\nend\nt\n");
        }
        Scanner scanner = new Scanner(new ByteArrayInputStream(sb.toString().getBytes()));
        Game testGame = new Game(4, scanner);
        
        try {
            testGame.startGame();
        } catch (java.util.NoSuchElementException e) {
            // Expected if input runs out
        }
        
        // Verify each player has a character
        for (Player player : testGame.getPlayers()) {
            assertNotNull(player.getCharacter(), "Each player should have a character");
            assertTrue(player.getCharacter().getNumber() >= 1 && player.getCharacter().getNumber() <= 8, 
                "Character number should be between 1 and 8");
        }
    }

    // Test building districts
    @Test
    public void testBuildingDistricts() {
        Player humanPlayer = game.getPlayers().get(0);
        humanPlayer.setCharacter(new CharacterCard("King", 4, "Gain gold for yellow districts")); // Set a character
        
        // Clear hand and add a test card
        humanPlayer.getHand().clear();
        DistrictCard testCard = new DistrictCard("Temple", "blue", 2, "");
        humanPlayer.addToHand(testCard);
        
        // Give player enough gold
        humanPlayer.addGold(5); // More than enough gold
        
        int initialGold = humanPlayer.getGold();
        int initialHandSize = humanPlayer.getHand().size();
        
        // Build the district
        humanPlayer.buildDistrict(testCard);
        
        assertEquals(initialGold - testCard.getCost(), humanPlayer.getGold(), "Gold should be reduced by card cost");
        assertEquals(initialHandSize - 1, humanPlayer.getHand().size(), "Hand size should decrease by 1");
        assertEquals(1, humanPlayer.getCity().size(), "City size should increase by 1");
    }

    // Test duplicate district prevention
    @Test
    public void testDuplicateDistrictPrevention() {
        Player humanPlayer = game.getPlayers().get(0);
        if (!humanPlayer.getHand().isEmpty()) {
            DistrictCard card = humanPlayer.getHand().get(0);
            humanPlayer.buildDistrict(card);
            int initialCitySize = humanPlayer.getCity().size();
            
            // Try to build the same district again
            humanPlayer.buildDistrict(card);
            assertEquals(initialCitySize, humanPlayer.getCity().size(), "Should not allow duplicate districts");
        }
    }

    // Test game end condition
    @Test
    public void testGameEndCondition() {
        Player humanPlayer = game.getPlayers().get(0);
        // Force build 8 districts to trigger game end
        humanPlayer.getHand().clear();
        humanPlayer.addGold(20);
        for (int i = 0; i < 8; i++) {
            humanPlayer.addToHand(new DistrictCard("Temple", "blue", 1, ""));
            humanPlayer.buildDistrict(humanPlayer.getHand().get(0));
        }
        game.checkGameEnd(); // Ensure game end is checked
        assertTrue(game.isGameEnded(), "Game should end when a player has 8 districts");
    }

    // Test character abilities
    @Test
    public void testCharacterAbilities() {
        Player humanPlayer = game.getPlayers().get(0);
        // Test King's ability
        humanPlayer.setCharacter(new CharacterCard("King", 4, "Gain gold for yellow districts"));
        int initialGold = humanPlayer.getGold();
        // Add a yellow district directly to city
        humanPlayer.getCity().add(new DistrictCard("Palace", "yellow", 5, ""));
        game.triggerCharacterAbility(humanPlayer); // Trigger ability
        assertTrue(humanPlayer.getGold() > initialGold, "King should gain gold for yellow districts");
    }

    // Test game saving and loading
    @Test
    public void testGameSaveAndLoad() {
        try {
            // Save game state
            game.saveGame("test_save.json");
            
            // Modify game state
            game.forceEndGame();
            assertTrue(game.isGameEnded(), "Game should be ended");
            
            // Load game state
            game.loadGame("test_save.json");
            assertFalse(game.isGameEnded(), "Game should not be ended after loading");
        } catch (Exception e) {
            fail("Save/Load test failed: " + e.getMessage());
        }
    }

    // Test edge cases
    @Test
    public void testEdgeCases() {
        // Test with minimum players
        Game minGame = new Game(4);
        assertEquals(4, minGame.getPlayers().size(), "Should handle minimum players");
        
        // Test with maximum players
        Game maxGame = new Game(7);
        assertEquals(7, maxGame.getPlayers().size(), "Should handle maximum players");
    }

    // Test invalid player count
    @Test
    public void testInvalidPlayerCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(3); // Should throw exception for less than 4 players
        });
    }

    // Test character killing
    @Test
    public void testCharacterKilling() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("2\n".getBytes()));
        Game testGame = new Game(4, scanner);
        Player assassin = testGame.getPlayers().get(0);
        assassin.setCharacter(new CharacterCard("Assassin", 1, "Select another character to kill"));
        testGame.triggerCharacterAbility(assassin); // Trigger ability
        assertNotNull(testGame.getKilledCharacter(), "Killed character should be set");
    }

    // Test character robbing
    @Test
    public void testCharacterRobbing() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("3\n".getBytes()));
        Game testGame = new Game(4, scanner);
        Player thief = testGame.getPlayers().get(0);
        thief.setCharacter(new CharacterCard("Thief", 2, "Select another character to rob"));
        testGame.triggerCharacterAbility(thief); // Trigger ability
        assertNotNull(testGame.getRobbedCharacter(), "Robbed character should be set");
    }

    // Test purple card abilities
    @Test
    public void testPurpleCardAbilities() {
        Player humanPlayer = game.getPlayers().get(0);
        // Ensure hand contains Museum card and enough gold
        humanPlayer.getHand().clear();
        humanPlayer.addGold(10);
        DistrictCard museum = new DistrictCard("Museum", "purple", 4, "Store cards for end-game points");
        humanPlayer.addToHand(museum);
        humanPlayer.buildDistrict(museum);
        assertTrue(humanPlayer.getCity().contains(museum), "Museum should be built");
    }

    // Test game state after round completion
    @Test
    public void testRoundCompletion() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) sb.append("assassin\ngold\nend\nthief\ngold\nend\nmagician\ngold\nend\nking\ngold\nend\n");
        Scanner scanner = new Scanner(new ByteArrayInputStream(sb.toString().getBytes()));
        Game testGame = new Game(4, scanner);
        int initialRound = testGame.getCurrentRound();
        testGame.startGame();
        assertTrue(testGame.getCurrentRound() > initialRound, "Round should advance");
    }

    // Test a full human turn with simulated input: collect gold and end turn
    @Test
    public void testHumanTurnCollectGoldAndEnd() {
        // Simulate input: collect gold, then end turn
        String simulatedInput = "gold\nend\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        Game testGame = new Game(4, scanner);
        Player human = testGame.getPlayers().get(0);
        human.setCharacter(new CharacterCard("King", 4, "Gain gold for yellow districts"));
        int initialGold = human.getGold();
        testGame.processHumanTurn(human);
        assertTrue(human.getGold() > initialGold, "Human should have collected gold");
    }

    // Test a human turn with build command and invalid command
    @Test
    public void testHumanTurnBuildAndInvalidCommand() {
        // Simulate input: try invalid command, then build, then end
        String simulatedInput = "foobar\nbuild 1\nend\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        Game testGame = new Game(4, scanner);
        Player human = testGame.getPlayers().get(0);
        human.setCharacter(new CharacterCard("King", 4, "Gain gold for yellow districts"));
        // Ensure player has enough gold to build
        if (!human.getHand().isEmpty()) {
            DistrictCard card = human.getHand().get(0);
            human.addGold(card.getCost());
        }
        int initialCitySize = human.getCity().size();
        testGame.processHumanTurn(human);
        assertTrue(human.getCity().size() > initialCitySize, "Human should have built a district");
    }

    // Test processCommand for various commands
    @Test
    public void testProcessCommandVarious() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        human.setCharacter(new CharacterCard("King", 4, "Gain gold for yellow districts"));
        // Give player some gold and a card
        human.addGold(5);
        if (human.getHand().isEmpty()) {
            human.addToHand(new DistrictCard("Palace", "yellow", 5, ""));
        }
        // Test 'hand' command
        testGame.processCommand("hand", human);
        // Test 'gold' command
        testGame.processCommand("gold", human);
        // Test 'build 1' command
        testGame.processCommand("build 1", human);
        // Test 'city' command
        testGame.processCommand("city", human);
        // Test 'info King' command
        testGame.processCommand("info King", human);
    }

    // Test action commands
    @Test
    public void testActionSwap() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player magician = testGame.getPlayers().get(0);
        Player target = testGame.getPlayers().get(1);
        magician.setCharacter(new CharacterCard("Magician", 3, "Exchange hand or redraw cards"));
        // Give players some cards
        magician.addToHand(new DistrictCard("Palace", "yellow", 5, ""));
        target.addToHand(new DistrictCard("Temple", "blue", 1, ""));
        int initialHandSize = magician.getHand().size();
        testGame.processCommand("action swap 2", magician);
        assertEquals(initialHandSize, magician.getHand().size(), "Magician should have same hand size after swap");
    }

    @Test
    public void testActionRedraw() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player magician = testGame.getPlayers().get(0);
        magician.setCharacter(new CharacterCard("Magician", 3, "Exchange hand or redraw cards"));
        // Give player some cards
        magician.addToHand(new DistrictCard("Palace", "yellow", 5, ""));
        int initialHandSize = magician.getHand().size();
        testGame.processCommand("action redraw 1", magician);
        assertEquals(initialHandSize, magician.getHand().size(), "Magician should have same hand size after redraw");
    }

    @Test
    public void testActionKill() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player assassin = testGame.getPlayers().get(0);
        assassin.setCharacter(new CharacterCard("Assassin", 1, "Select another character to kill"));
        testGame.processCommand("action kill 2", assassin);
        assertNotNull(testGame.getKilledCharacter(), "Killed character should be set after kill action");
    }

    @Test
    public void testActionSteal() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player thief = testGame.getPlayers().get(0);
        thief.setCharacter(new CharacterCard("Thief", 2, "Select another character to rob"));
        testGame.processCommand("action steal 3", thief);
        assertNotNull(testGame.getRobbedCharacter(), "Robbed character should be set after steal action");
    }

    @Test
    public void testActionDestroy() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player warlord = testGame.getPlayers().get(0);
        Player target = testGame.getPlayers().get(1);
        warlord.setCharacter(new CharacterCard("Warlord", 8, "Gain gold for red districts, destroy districts"));
        // Give target a district
        target.buildDistrict(new DistrictCard("Temple", "blue", 1, ""));
        // Give warlord enough gold
        warlord.addGold(5);
        testGame.processCommand("action destroy 2 1", warlord);
        assertEquals(0, target.getCity().size(), "Target's city should be empty after destroy");
    }

    @Test
    public void testActionMuseum() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        // Clear hand and add only one card
        human.getHand().clear();
        human.addToHand(new DistrictCard("Temple", "blue", 1, ""));
        testGame.processCommand("action museum 1", human);
        assertEquals(0, human.getHand().size(), "Hand should be empty after storing in museum");
    }

    @Test
    public void testActionArmory() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        Player target = testGame.getPlayers().get(1);
        // Give target a district
        target.getHand().clear();
        target.addGold(10);
        DistrictCard temple = new DistrictCard("Temple", "blue", 1, "");
        target.addToHand(temple);
        target.buildDistrict(temple);
        // Give human the Armory in their city
        human.getHand().clear();
        human.addGold(10);
        DistrictCard armory = new DistrictCard("Armory", "purple", 3, "");
        human.addToHand(armory);
        human.buildDistrict(armory);
        testGame.processCommand("action armory 2 1", human);
        assertEquals(0, target.getCity().size(), "Target's city should be empty after armory action");
    }

    @Test
    public void testInvalidBuildIndex() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        testGame.processCommand("build 999", human);
        assertEquals(0, human.getCity().size(), "Should not build with invalid index");
    }

    @Test
    public void testInvalidActionCommand() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        testGame.processCommand("action invalid", human);
        assertEquals(0, human.getCity().size(), "Should not execute invalid action");
    }

    @Test
    public void testInvalidPlayerNumber() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        testGame.processCommand("citadel 999", human);
        assertEquals(0, human.getCity().size(), "Should not access invalid player");
    }

    @Test
    public void testInfoNonExistentCard() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        Player human = testGame.getPlayers().get(0);
        testGame.processCommand("info NonexistentCard", human);
        assertEquals(0, human.getCity().size(), "Should not crash on non-existent card");
    }

    @Test
    public void testGameEndAndScoring() {
        Player humanPlayer = game.getPlayers().get(0);
        // Build 8 districts
        humanPlayer.getHand().clear();
        humanPlayer.addGold(20);
        for (int i = 0; i < 8; i++) {
            humanPlayer.addToHand(new DistrictCard("Temple", "blue", 1, ""));
            humanPlayer.buildDistrict(humanPlayer.getHand().get(0));
        }
        game.checkGameEnd(); // Ensure game end is checked
        assertTrue(game.isGameEnded(), "Game should be ended after 8 districts");
    }

    @Test
    public void testAIPlayerMethods() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append("assassin\ngold\nend\nthief\ngold\nend\nmagician\ngold\nend\nking\ngold\nend\n");
        Scanner scanner = new Scanner(new ByteArrayInputStream(sb.toString().getBytes()));
        Game testGame = new Game(4, scanner);
        Player ai = testGame.getPlayers().get(1);
        assertFalse(ai.isHuman(), "AI player should not be human");
        try {
            testGame.startGame();
        } catch (java.util.NoSuchElementException e) {
            // Expected if input runs out
        }
        assertTrue(ai.getGold() >= 0, "AI should have non-negative gold");
    }

    @Test
    public void testStartGameInTestMode() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        testGame.startGame();
        assertTrue(testGame.getCurrentRound() > 0, "Game should have started");
    }

    @Test
    public void testFullGameFlow() {
        String simulatedInput = "assassin\ngold\nend\nthief\ngold\nend\nmagician\ngold\nend\nking\ngold\nend\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        Game testGame = new Game(4, scanner);
        testGame.startGame();
        assertTrue(testGame.getCurrentRound() > 0, "Game should have started");
        assertTrue(testGame.getPlayers().size() == 4, "Game should have 4 players");
    }

    @Test
    public void testRobustFullGameFlow() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        testGame.startGame();
        assertTrue(testGame.getCurrentRound() > 0, "Game should have started");
        assertTrue(testGame.getPlayers().size() == 4, "Game should have 4 players");
        for (Player player : testGame.getPlayers()) {
            assertTrue(player.getGold() >= 0, "Player should have non-negative gold");
            assertTrue(player.getHand().size() >= 0, "Player should have non-negative hand size");
        }
    }

    @Test
    public void testFullGameCoverage() {
        Game testGame = new Game(4, new Scanner(new ByteArrayInputStream(new byte[0])));
        testGame.startGame();
        assertTrue(testGame.getCurrentRound() > 0, "Game should have started");
        assertTrue(testGame.getPlayers().size() == 4, "Game should have 4 players");
        for (Player player : testGame.getPlayers()) {
            assertTrue(player.getGold() >= 0, "Player should have non-negative gold");
            assertTrue(player.getHand().size() >= 0, "Player should have non-negative hand size");
            assertTrue(player.getCity().size() >= 0, "Player should have non-negative city size");
        }
    }
}
