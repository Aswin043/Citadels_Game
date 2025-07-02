/*
    USYD CODE CITATION ACKNOWLEDGEMENT
    I HEARBY ACKNOWLEDGE THE USE OF AI TOOLS IN THE DEVELOPMENT OF THIS PROJECT.
*/
package citadels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Main game class for Citadels, a strategic card game where players build districts in their city.
 * This class handles the core game mechanics including:
 * <ul>
 *     <li>Game initialization and setup</li>
 *     <li>Character selection and turn management</li>
 *     <li>Player actions and interactions</li>
 *     <li>District building and scoring</li>
 *     <li>Game state management and persistence</li>
 * </ul>
 * 
 * The game supports 4-7 players, with one human player and the rest being AI-controlled.
 * Players take turns selecting characters, collecting resources, and building districts
 * to create the most valuable city.
 * 
 * @author Citadels Game Team
 * @version 1.0
 */
public class Game {
    /** Storage for cards under the Museum purple district */
    private Map<Player, List<DistrictCard>> museumStorage = new HashMap<>();
    
    /** Flag indicating if Bell Tower's early end condition is active */
    private boolean bellTowerEarlyEnd = false;
    
    /** Flag indicating if Bell Tower's early end has been announced */
    private boolean bellTowerAnnounced = false;
    
    /** List of all players in the game */
    private List<Player> players;
    
    /** Deck of district cards available for drawing */
    private List<DistrictCard> districtDeck;
    
    /** Pile of discarded district cards */
    private List<DistrictCard> discardPile;
    
    /** Deck of character cards */
    private List<CharacterCard> characterDeck;
    
    /** List of character cards available for selection in current round */
    private List<CharacterCard> availableCharacters;
    
    /** Current round number of the game */
    private int currentRound;
    
    /** Flag indicating if the game has ended */
    private boolean gameEnded;
    
    /** Scanner for reading user input */
    private Scanner scanner;
    
    /** Character that was killed by the Assassin in current round */
    private CharacterCard killedCharacter;
    
    /** Character that was robbed by the Thief in current round */
    private CharacterCard robbedCharacter;
    
    /** Flag for enabling debug mode to show AI information */
    private boolean debugMode = false;

    /** Add/modify fields for tracking destroyed districts and crown switching */
    private DistrictCard lastDestroyedDistrict = null;
    private Player lastDestroyedDistrictOwner = null;
    private boolean crownSwitched = false;

    /** Laboratory used map */
    private Map<Player, Boolean> laboratoryUsed = new HashMap<>();
    
    /** Smithy used map */
    private Map<Player, Boolean> smithyUsed = new HashMap<>();

    /**
     * Constructs a new Game instance with the specified number of players and a custom Scanner (for testability).
     * @param numPlayers The number of players in the game (must be between 4 and 7)
     * @param scanner The Scanner to use for user input
     * @throws IllegalArgumentException if numPlayers is not between 4 and 7
     */
    public Game(int numPlayers, Scanner scanner) {
        if (numPlayers < 4 || numPlayers > 7) {
            throw new IllegalArgumentException("Number of players must be between 4 and 7");
        }
        
        this.players = new ArrayList<>();
        this.districtDeck = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.characterDeck = new ArrayList<>();
        this.availableCharacters = new ArrayList<>();
        this.currentRound = 1;
        this.gameEnded = false;
        this.scanner = scanner;

        System.out.println("\nInitial Setup");
        System.out.println("Shuffling deck...");
        // Initialize players
        for (int i = 0; i < numPlayers; i++) {
            if (i == 0) {
                players.add(new Player(i + 1, true)); // Player 1 is human
            } else {
                players.add(new AIPlayer(i + 1)); // All others are AIPlayer
            }
        }

        // Set initial crown holder (random)
        int crownedPlayer = (int)(Math.random() * numPlayers);
        players.get(crownedPlayer).setHasCrown(true);
        System.out.println("Player " + (crownedPlayer + 1) + " has been randomly chosen to hold the crown.");

        System.out.println("Adding characters...");
        // Load district cards
        loadDistrictCards();

        // Initialize character cards
        initializeCharacterCards();

        // Shuffle decks
        Collections.shuffle(districtDeck);
        Collections.shuffle(characterDeck);

        System.out.println("Dealing cards and gold...");
        // Deal initial cards and gold
        dealInitialCards();
    }

    /**
     * Constructs a new Game instance with the specified number of players.
     * Initializes the game state, creates players, and sets up the initial game board.
     *
     * @param numPlayers The number of players in the game (must be between 4 and 7)
     * @throws IllegalArgumentException if numPlayers is not between 4 and 7
     */
    public Game(int numPlayers) {
        this(numPlayers, new Scanner(System.in));
    }

    /**
     * Loads district cards from the resource file and initializes the district deck.
     * Cards are loaded from a TSV file containing card information.
     */
    private void loadDistrictCards() {
        try (InputStream is = getClass().getResourceAsStream("/citadels/cards.tsv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String name = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                String color = parts[2];
                int cost = Integer.parseInt(parts[3]);
                String specialAbility = parts.length > 4 ? parts[4] : "";

                for (int i = 0; i < quantity; i++) {
                    districtDeck.add(new DistrictCard(name, color, cost, specialAbility));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading district cards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes the character deck with all available character cards.
     * Each character has a unique number and special ability.
     */
    private void initializeCharacterCards() {
        characterDeck.add(new CharacterCard("Assassin", 1, "Select another character to kill"));
        characterDeck.add(new CharacterCard("Thief", 2, "Select another character to rob"));
        characterDeck.add(new CharacterCard("Magician", 3, "Exchange hand or redraw cards"));
        characterDeck.add(new CharacterCard("King", 4, "Gain gold for yellow districts"));
        characterDeck.add(new CharacterCard("Bishop", 5, "Gain gold for blue districts"));
        characterDeck.add(new CharacterCard("Merchant", 6, "Gain gold for green districts"));
        characterDeck.add(new CharacterCard("Architect", 7, "Draw extra cards, build up to 3 districts"));
        characterDeck.add(new CharacterCard("Warlord", 8, "Gain gold for red districts, destroy districts"));
    }

    /**
     * Deals initial cards and gold to all players at the start of the game.
     * Each player receives 2 gold and 4 district cards.
     */
    private void dealInitialCards() {
        for (Player player : players) {
            // Reset gold to 0 first, then add exactly 2 gold
            player.addGold(-player.getGold()); // Reset to 0
            player.addGold(2); // Add exactly 2 gold
            
            // Deal 4 district cards
            for (int i = 0; i < 4; i++) {
                if (!districtDeck.isEmpty()) {
                    player.addToHand(districtDeck.remove(0));
                }
            }
        }
    }

    /**
     * Starts the main game loop.
     * Manages rounds, character selection, and player turns until the game ends.
     */
    public void startGame() {
        
        System.out.println("\nStarting Citadels with " + players.size() + " players...");
        System.out.println("You are player 1");

        while (!gameEnded) {
            System.out.println("\n================================");
            System.out.println("ROUND " + currentRound);
            System.out.println("================================");
            
            // Character Selection Phase
            System.out.println("\n================================");
            System.out.println("SELECTION PHASE");
            System.out.println("================================");
            
            try {
                characterSelectionPhase();
            } catch (Exception e) {
                System.err.println("Error in character selection phase: " + e.getMessage());
                gameEnded = true;
                break;
            }
            
            // Turn Phase
            System.out.println("\nTURN PHASE");
            try {
                turnPhase();
            } catch (Exception e) {
                System.err.println("Error in turn phase: " + e.getMessage());
                gameEnded = true;
                break;
            }
            
            // Check for game end
            checkGameEnd();
            
            if (!gameEnded) {
                currentRound++;
            }
        }

        endGame();
    }

    /**
     * Handles the character selection phase of each round.
     * Players choose characters in order based on who holds the crown.
     */
    private void characterSelectionPhase() {
        
        // Reset available characters
        availableCharacters.clear();
        availableCharacters.addAll(characterDeck);
        Collections.shuffle(availableCharacters);
        
        int numPlayers = players.size();
        int faceUpToRemove = 0;
        int faceDownToRemove = 1;
        boolean special7PlayerRule = false;
        
        if (numPlayers == 4) {
            faceUpToRemove = 2;
            faceDownToRemove = 1;
        } else if (numPlayers == 5) {
            faceUpToRemove = 1;
            faceDownToRemove = 1;
        } else if (numPlayers == 6) {
            faceUpToRemove = 0;
            faceDownToRemove = 1;
        } else if (numPlayers == 7) {
            faceUpToRemove = 0;
            faceDownToRemove = 1;
            special7PlayerRule = true;
        }
        
        // Remove face-down card(s)
        List<CharacterCard> faceDownCards = new ArrayList<>();
        for (int i = 0; i < faceDownToRemove; i++) {
            if (!availableCharacters.isEmpty()) {
                CharacterCard faceDown = availableCharacters.remove(0);
                faceDownCards.add(faceDown);
                System.out.println("A mystery character was removed.");
                waitForContinue();
            }
        }
        
        // Remove face-up cards
        for (int i = 0; i < faceUpToRemove; i++) {
            if (!availableCharacters.isEmpty()) {
                CharacterCard removed;
                boolean validRemoval = false;
                
                do {
                    removed = availableCharacters.remove(0);
                    if (removed.getName().equals("King")) {
                        System.out.println("The King cannot be visibly removed, trying again..");
                        availableCharacters.add(removed);
                        Collections.shuffle(availableCharacters);
                        waitForContinue();
                    } else {
                        validRemoval = true;
                        System.out.println(removed.getName() + " was removed.");
                    }
                } while (!validRemoval);
                waitForContinue();
            }
        }
        
        // Find crown holder and start selection from there
        int startIndex = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).hasCrown()) {
                startIndex = i;
                System.out.println("Player " + (i + 1) + " is the crowned player and goes first.");
                waitForContinue();
                break;
            }
        }
        
        // Let players choose characters in order
        for (int i = 0; i < players.size(); i++) {
            int playerIndex = (startIndex + i) % players.size();
            Player player = players.get(playerIndex);
            
            if (player.isHuman()) {
                System.out.println("\nChoose your character. Available characters:");
                for (CharacterCard card : availableCharacters) {
                    System.out.print(card.getName() + ", ");
                }
                System.out.println();
                
                String choice = "king"; // Default to king if input fails
                try {
                    do {
                        choice = scanner.next().toLowerCase();
                        CharacterCard chosen = null;
                        for (CharacterCard card : availableCharacters) {
                            if (card.getName().toLowerCase().equals(choice)) {
                                chosen = card;
                                break;
                            }
                        }
                        if (chosen != null) {
                            availableCharacters.remove(chosen);
                            player.setCharacter(chosen);
                            System.out.println("Player " + player.getPlayerNumber() + " chose a character.");
                            break;
                        } else {
                            System.out.println("Invalid character choice. Try again.");
                        }
                    } while (true);
                } catch (Exception e) {
                    // If we get an exception, pick the first available character
                    if (!availableCharacters.isEmpty()) {
                        CharacterCard chosen = availableCharacters.remove(0);
                        player.setCharacter(chosen);
                        System.out.println("Player " + player.getPlayerNumber() + " chose a character (fallback).");
                    }
                }
            } else {
                // AI character selection
                int choice = (int) (Math.random() * availableCharacters.size());
                CharacterCard chosen = availableCharacters.remove(choice);
                player.setCharacter(chosen);
                System.out.println("Player " + player.getPlayerNumber() + " chose a character.");
            }
            waitForContinue();
        }

        // After setting new crown holder, for each player with Throne Room, add 1 gold and print message
        for (Player p : players) {
            if (p.getCity().stream().anyMatch(c -> c.getName().equals("Throne Room"))) {
                p.addGold(1);
                System.out.println("Throne Room: Player " + p.getPlayerNumber() + " receives 1 gold for crown switch.");
            }
        }
    }

    /**
     * Waits for user input to continue the game.
     * Processes any commands entered by the user.
     */
    private void waitForContinue() {
        System.out.print("> ");
        String commandLine = scanner.nextLine().trim();
        
        if (commandLine.isEmpty() || commandLine.equalsIgnoreCase("t")) {
            return;
        } else {
            processCommand(commandLine, null);
        }
    }

    /**
     * Handles the turn phase of each round.
     * Processes turns for each character in numerical order.
     */
    private void turnPhase() {
        
        System.out.println("\nCharacter choosing is over, action round will now begin.");
        System.out.println("\n================================");
        System.out.println("TURN PHASE");
        System.out.println("================================");
        
        for (int i = 1; i <= 8; i++) {
            boolean characterFound = false;
            Player characterPlayer = null;
            
            for (Player player : players) {
                if (player.getCharacter() != null && player.getCharacter().getNumber() == i) {
                    characterFound = true;
                    characterPlayer = player;
                    break;
                }
            }
            
            System.out.println(i + ": " + getCharacterName(i));
            if (!characterFound) {
                System.out.println("No one is the " + getCharacterName(i));
                try {
                    waitForContinueWithDebug();
                } catch (Exception e) {
                    // If we get an exception, just continue
                    continue;
                }
                continue;
            }
            
            // Only reveal effects when the affected character's turn comes up
            if (characterPlayer.getCharacter() == killedCharacter) {
                boolean hasHospital = characterPlayer.getCity().stream().anyMatch(c -> c.getName().equals("Hospital"));
                if (hasHospital) {
                    System.out.println("You were assassinated, but Hospital lets you take a basic action (no build or power).");
                    // Only allow gold/cards choice
                    System.out.println("Collect 2 gold or draw two cards and pick one [gold/cards].");
                    String choice = "gold";
                    try {
                        choice = scanner.next().toLowerCase();
                        scanner.nextLine();
                    } catch (Exception e) {}
                    if (choice.equals("gold")) {
                        characterPlayer.addGold(2);
                        System.out.println("Player " + characterPlayer.getPlayerNumber() + " received 2 gold.");
                    } else if (choice.equals("cards")) {
                        try {
                            drawAndChooseCard(characterPlayer);
                        } catch (Exception e) {
                            System.err.println("Error drawing cards: " + e.getMessage());
                        }
                    }
                    try { waitForContinueWithDebug(); } catch (Exception e) {}
                    continue;
                }
                if (characterPlayer.isHuman()) {
                    System.out.println("You have been killed by the Assassin! Your turn is skipped.");
                }
                try {
                    waitForContinueWithDebug();
                } catch (Exception e) {
                    // If we get an exception, just continue
                    continue;
                }
                continue;
            }
            if (characterPlayer.getCharacter() == robbedCharacter) {
                // Find the thief and transfer gold
                for (Player p : players) {
                    if (p.getCharacter() != null && p.getCharacter().getName().equals("Thief")) {
                        int goldStolen = characterPlayer.getGold();
                        p.addGold(goldStolen);
                        characterPlayer.addGold(-goldStolen);
                        if (characterPlayer.isHuman()) {
                            System.out.println("You have been robbed by the Thief! " + goldStolen + " gold stolen.");
                        }
                        break;
                    }
                }
            }
            
            try {
                if (characterPlayer.isHuman()) {
                    processHumanTurn(characterPlayer);
                } else {
                    processAITurn(characterPlayer);
                }
            } catch (Exception e) {
                System.err.println("Error processing turn: " + e.getMessage());
                // Continue to next turn
            }
        }
    }

    private void waitForContinueWithDebug() {
        try {
            if (debugMode) {
                showAllAIPlayers();
            }
            System.out.print("> ");
            String commandLine = scanner.nextLine().trim();
            
            if (commandLine.toLowerCase().equals("t")) {
                return;
            } else if (commandLine.toLowerCase().equals("debug")) {
                debugMode = !debugMode;
                System.out.println("Debug mode is now " + (debugMode ? "ON" : "OFF"));
            } else {
                processCommand(commandLine, null);
            }
        } catch (Exception e) {
            // If we get an exception, just return
            return;
        }
    }

    // Show all AI hands and gold if debugMode is on
    private void showAllAIPlayers() {
        for (Player player : players) {
            if (!player.isHuman()) {
                System.out.println("Player " + player.getPlayerNumber() + " (" + (player.getCharacter() != null ? player.getCharacter().getName() : "No character") + ")");
                System.out.println("  Gold: " + player.getGold());
                System.out.println("  Hand:");
                for (DistrictCard card : player.getHand()) {
                    System.out.println("    - " + card);
                }
            }
        }
    }

    private String getCharacterName(int number) {
        switch (number) {
            case 1: return "Assassin";
            case 2: return "Thief";
            case 3: return "Magician";
            case 4: return "King";
            case 5: return "Bishop";
            case 6: return "Merchant";
            case 7: return "Architect";
            case 8: return "Warlord";
            default: return "Unknown";
        }
    }

    /**
     * Processes a turn for an AI-controlled player.
     * Makes decisions about collecting gold, drawing cards, and building districts.
     *
     * @param player The AI player whose turn is being processed
     */
    private void processAITurn(Player player) {
        // AI decision making for basic turn action
        if (shouldAITakeGold(player)) {
            player.addGold(2);
        } else {
            drawAndChooseCardAI(player);
        }
        
        // AI decision making for building
        int maxBuilds = player.getCharacter().getName().equals("Architect") ? 3 : 1;
        int buildsThisTurn = 0;
        while (buildsThisTurn < maxBuilds) {
            DistrictCard bestCard = findBestCardToBuild(player);
            if (bestCard != null && player.getGold() >= bestCard.getCost()) {
                player.buildDistrict(bestCard);
                player.getHand().remove(bestCard);
                // Always show built districts (public info)
                System.out.println("Player " + player.getPlayerNumber() + " built " + bestCard);
                buildsThisTurn++;
            } else {
                break;
            }
        }
        
        // AI decision making for special ability
        if (shouldAIUseSpecialAbility(player)) {
            useSpecialAbilityAI(player);
        }
    }

    // Human start-of-turn abilities
    private void handleStartOfTurnAbilitiesHuman(Player player) {
        String characterName = player.getCharacter().getName();
        if (characterName.equals("Assassin")) {
            System.out.println("Choose a character to kill (2-8):");
            String input = scanner.nextLine();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 2 && choice <= 8) {
                    String targetCharacter = getCharacterName(choice);
                    killedCharacter = findCharacterCard(targetCharacter);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. No character was killed.");
            }
        }
        if (characterName.equals("Thief")) {
            System.out.println("Choose a character to rob (3-8):");
            String input = scanner.nextLine();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 3 && choice <= 8) {
                    String targetCharacter = getCharacterName(choice);
                    robbedCharacter = findCharacterCard(targetCharacter);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. No character was robbed.");
            }
        }
        // Handle district-based gold bonuses (only after first turn)
        if (currentRound > 1) {
            int bonusGold = 0;
            switch (characterName) {
                case "King":
                    bonusGold = countDistrictsByColor(player, "yellow");
                    break;
                case "Bishop":
                    bonusGold = countDistrictsByColor(player, "blue");
                    break;
                case "Merchant":
                    bonusGold = countDistrictsByColor(player, "green") + 1; // +1 for merchant bonus
                    break;
                case "Warlord":
                    bonusGold = countDistrictsByColor(player, "red");
                    break;
            }
            if (bonusGold > 0) {
                player.addGold(bonusGold);
                System.out.println("You received " + bonusGold + " gold from your character's ability.");
            }
        }
    }

    private void drawAndChooseCard(Player player) {
        boolean hasObservatory = player.getCity().stream().anyMatch(c -> c.getName().equals("Observatory"));
        boolean hasLibrary = player.getCity().stream().anyMatch(c -> c.getName().equals("Library"));
        int numToDraw = hasObservatory ? 3 : 2;
        List<DistrictCard> drawnCards = new ArrayList<>();
        for (int i = 0; i < numToDraw; i++) {
            if (!districtDeck.isEmpty()) {
                drawnCards.add(districtDeck.remove(0));
            }
        }
        System.out.println("Drawn cards:");
        for (int i = 0; i < drawnCards.size(); i++) {
            System.out.println((i + 1) + ". " + drawnCards.get(i));
        }
        if (hasLibrary && drawnCards.size() == 2) {
            for (DistrictCard card : drawnCards) {
                player.addToHand(card);
                System.out.println("You kept " + card);
            }
            return;
        }
        System.out.println("Choose a card to keep [1-" + drawnCards.size() + "]:");
        int choice = 0; // Default to first card if input fails
        try {
            do {
                while (!scanner.hasNextInt()) {
                    System.out.println("Please enter a valid number.");
                    scanner.next();
                }
                choice = scanner.nextInt() - 1;
                scanner.nextLine(); // Consume the newline
            } while (choice < 0 || choice >= drawnCards.size());
        } catch (Exception e) {
            choice = 0;
        }
        player.addToHand(drawnCards.get(choice));
        System.out.println("You kept " + drawnCards.get(choice));
        // Return other card(s) to deck (bottom if Observatory)
        for (int i = 0; i < drawnCards.size(); i++) {
            if (i != choice) {
                if (hasObservatory) {
                    districtDeck.add(drawnCards.get(i)); // bottom
                } else {
                    districtDeck.add(0, drawnCards.get(i)); // top
                }
            }
        }
        Collections.shuffle(districtDeck);
    }

    /**
     * Processes a turn for a human player.
     * Handles user input and executes player actions.
     *
     * @param player The human player whose turn is being processed
     */
    public void processHumanTurn(Player player) {
        
        System.out.println("Your turn.");
        
        // Handle character-specific start of turn abilities (human)
        try {
            handleStartOfTurnAbilitiesHuman(player);
        } catch (Exception e) {
            System.err.println("Error in start of turn abilities: " + e.getMessage());
        }
        
        // Basic turn actions (gold or cards)
        System.out.println("Collect 2 gold or draw two cards and pick one [gold/cards].");
        String choice = "gold"; // Default to gold if input fails
        try {
            choice = scanner.next().toLowerCase();
            scanner.nextLine(); // Consume the newline
        } catch (Exception e) {
            // If we get an exception, use default value
        }
        
        if (choice.equals("gold")) {
            player.addGold(2);
            System.out.println("Player " + player.getPlayerNumber() + " received 2 gold.");
        } else if (choice.equals("cards")) {
            try {
                drawAndChooseCard(player);
            } catch (Exception e) {
                System.err.println("Error drawing cards: " + e.getMessage());
            }
        }
        
        boolean turnEnded = false;
        while (!turnEnded) {
            try {
                System.out.print("> ");
                String commandLine = scanner.nextLine().trim();
                if (commandLine.isEmpty()) {
                    commandLine = scanner.nextLine().trim();
                }
                
                if (commandLine.toLowerCase().equals("end")) {
                    System.out.println("You ended your turn.");
                    turnEnded = true;
                } else {
                    processCommand(commandLine, player);
                }
            } catch (Exception e) {
                // If we get an exception, end the turn
                turnEnded = true;
            }
        }

        // At the end of processHumanTurn (before turnEnded = true):
        if (turnEnded) {
            // Poor House
            boolean hasPoorHouse = player.getCity().stream().anyMatch(c -> c.getName().equals("Poor House"));
            if (hasPoorHouse && player.getGold() == 0) {
                player.addGold(1);
                System.out.println("Poor House: You had no gold, so you receive 1 gold.");
            }
            // Park
            boolean hasPark = player.getCity().stream().anyMatch(c -> c.getName().equals("Park"));
            if (hasPark && player.getHand().isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    if (!districtDeck.isEmpty()) {
                        player.addToHand(districtDeck.remove(0));
                    }
                }
                System.out.println("Park: You had no cards, so you draw 2 cards.");
            }
        }
    }

    /**
     * Processes a command entered by the user.
     *
     * @param commandLine The command entered by the user
     * @param currentPlayer The player who entered the command, or null if not during a turn
     */
    public void processCommand(String commandLine, Player currentPlayer) {
        String[] parts = commandLine.split("\\s+");
        String command = parts[0].toLowerCase();

        // Null check for commands that require a player
        if (currentPlayer == null && (
            command.equals("hand") || command.equals("build") || command.equals("action") || command.equals("end")
        )) {
            System.out.println("No player context for this command.");
            return;
        }
        
        switch (command) {
            case "hand":
                if (currentPlayer != null && currentPlayer.isHuman()) {
                    showHand(currentPlayer);
                } else {
                    System.out.println("You can only view your own hand.");
                }
                break;
            case "gold":
                if (parts.length > 1) {
                    int p = parsePlayerNumber(parts[1]);
                    if (p != -1) {
                        System.out.println("Player " + p + " has " + players.get(p-1).getGold() + " gold.");
                    }
                } else if (currentPlayer != null && currentPlayer.isHuman()) {
                    System.out.println("You have " + currentPlayer.getGold() + " gold.");
                }
                break;
            case "build":
                if (currentPlayer != null && currentPlayer.isHuman()) {
                    if (parts.length > 1) {
                        int buildIndex = Integer.parseInt(parts[1]) - 1;
                        if (buildIndex >= 0 && buildIndex < currentPlayer.getHand().size()) {
                            buildDistrictWithDuplicateCheck(currentPlayer, buildIndex);
                        }
                    }
                } else {
                    System.out.println("You can only build during your turn.");
                }
                break;
            case "citadel":
            case "list":
            case "city":
                int cityPlayer = 1;
                if (parts.length > 1) {
                    int p = parsePlayerNumber(parts[1]);
                    if (p != -1) cityPlayer = p;
                }
                showCity(players.get(cityPlayer-1));
                break;
            case "action":
                if (currentPlayer != null && currentPlayer.isHuman()) {
                    if (parts.length < 2) {
                        showActionInfo(currentPlayer);
                    } else {
                        String subCommand = parts[1].toLowerCase();
                        switch (subCommand) {
                            case "swap":
                                if (!currentPlayer.getCharacter().getName().equals("Magician")) {
                                    System.out.println("Only the Magician can swap hands.");
                                    break;
                                }
                                if (parts.length < 3) {
                                    System.out.println("Usage: action swap <player number>");
                                } else {
                                    int targetPlayerNum = parsePlayerNumber(parts[2]);
                                    if (targetPlayerNum != -1 && targetPlayerNum != currentPlayer.getPlayerNumber()) {
                                        Player targetPlayer = players.get(targetPlayerNum - 1);
                                        List<DistrictCard> tempHand = new ArrayList<>(currentPlayer.getHand());
                                        currentPlayer.getHand().clear();
                                        currentPlayer.getHand().addAll(targetPlayer.getHand());
                                        targetPlayer.getHand().clear();
                                        targetPlayer.getHand().addAll(tempHand);
                                        System.out.println("Swapped hands with Player " + targetPlayerNum);
                                    } else {
                                        System.out.println("Invalid player number or cannot swap with yourself.");
                                    }
                                }
                                break;
                            case "redraw":
                                if (!currentPlayer.getCharacter().getName().equals("Magician")) {
                                    System.out.println("Only the Magician can redraw cards.");
                                    break;
                                }
                                if (parts.length < 3) {
                                    System.out.println("Usage: action redraw <id1,id2,id3,...>");
                                } else {
                                    String[] cardIds = parts[2].split(",");
                                    List<Integer> indices = new ArrayList<>();
                                    for (String id : cardIds) {
                                        try {
                                            int index = Integer.parseInt(id.trim()) - 1;
                                            if (index >= 0 && index < currentPlayer.getHand().size()) {
                                                indices.add(index);
                                            }
                                        } catch (NumberFormatException e) {
                                            System.out.println("Invalid card ID: " + id);
                                        }
                                    }
                                    if (!indices.isEmpty()) {
                                        indices.sort((a, b) -> b - a);
                                        for (int index : indices) {
                                            if (!districtDeck.isEmpty()) {
                                                currentPlayer.getHand().remove(index);
                                                currentPlayer.addToHand(districtDeck.remove(0));
                                            }
                                        }
                                        System.out.println("Redrew " + indices.size() + " cards.");
                                    }
                                }
                                break;
                            case "kill":
                                if (!currentPlayer.getCharacter().getName().equals("Assassin")) {
                                    System.out.println("Only the Assassin can kill characters.");
                                    break;
                                }
                                if (parts.length < 3) {
                                    System.out.println("Usage: action kill <character number>");
                                } else {
                                    try {
                                        int targetChar = Integer.parseInt(parts[2]);
                                        if (targetChar >= 2 && targetChar <= 8) {
                                            killedCharacter = findCharacterCard(getCharacterName(targetChar));
                                            System.out.println("You chose to kill the " + killedCharacter.getName());
                                        } else {
                                            System.out.println("Invalid character number. Choose between 2 and 8.");
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid character number.");
                                    }
                                }
                                break;
                            case "steal":
                                if (!currentPlayer.getCharacter().getName().equals("Thief")) {
                                    System.out.println("Only the Thief can steal from characters.");
                                    break;
                                }
                                if (parts.length < 3) {
                                    System.out.println("Usage: action steal <character number>");
                                } else {
                                    try {
                                        int targetChar = Integer.parseInt(parts[2]);
                                        if (targetChar >= 3 && targetChar <= 8) {
                                            robbedCharacter = findCharacterCard(getCharacterName(targetChar));
                                            System.out.println("You chose to steal from the " + robbedCharacter.getName());
                                        } else {
                                            System.out.println("Invalid character number. Choose between 3 and 8.");
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid character number.");
                                    }
                                }
                                break;
                            case "destroy":
                                if (!currentPlayer.getCharacter().getName().equals("Warlord")) {
                                    System.out.println("Only the Warlord can destroy districts.");
                                    break;
                                }
                                if (parts.length < 4) {
                                    System.out.println("Usage: action destroy <player number> <district number>");
                                    break;
                                }
                                try {
                                    int targetPlayerNum = parsePlayerNumber(parts[2]);
                                    if (targetPlayerNum != -1 && targetPlayerNum != currentPlayer.getPlayerNumber()) {
                                        Player targetPlayer = players.get(targetPlayerNum - 1);
                                        int districtIndex = Integer.parseInt(parts[3]) - 1;
                                        if (districtIndex >= 0 && districtIndex < targetPlayer.getCity().size()) {
                                            DistrictCard district = targetPlayer.getCity().get(districtIndex);
                                            // --- Keep: cannot be destroyed ---
                                            if (district.getName().equals("Keep")) {
                                                System.out.println("Keep cannot be destroyed by the Warlord.");
                                                break;
                                            }
                                            // --- Great Wall: +1 cost ---
                                            boolean hasGreatWall = targetPlayer.getCity().stream().anyMatch(c -> c.getName().equals("Great Wall"));
                                            int destroyCost = district.getCost() - 1 + (hasGreatWall ? 1 : 0);
                                            if (currentPlayer.getGold() >= destroyCost) {
                                                currentPlayer.addGold(-destroyCost);
                                                targetPlayer.getCity().remove(districtIndex);
                                                System.out.println("Destroyed " + district.getName() + " in Player " + targetPlayerNum + "'s city.");
                                                // --- Graveyard: recover destroyed district ---
                                                boolean hasGraveyard = targetPlayer.getCity().stream().anyMatch(c -> c.getName().equals("Graveyard"));
                                                boolean isWarlord = targetPlayer.getCharacter() != null && targetPlayer.getCharacter().getName().equals("Warlord");
                                                if (hasGraveyard && !isWarlord && targetPlayer.getGold() >= 1) {
                                                    System.out.println("Player " + targetPlayerNum + " may pay 1 gold to recover the destroyed district (Graveyard). (yes/no)");
                                                    String ans = scanner.next().trim().toLowerCase();
                                                    if (ans.startsWith("y")) {
                                                        targetPlayer.addGold(-1);
                                                        targetPlayer.addToHand(district);
                                                        System.out.println("Recovered " + district.getName() + " to hand (Graveyard).");
                                                    }
                                                }
                                            } else {
                                                System.out.println("Not enough gold to destroy this district.");
                                            }
                                        } else {
                                            System.out.println("Invalid district number.");
                                        }
                                    } else {
                                        System.out.println("Invalid player number or cannot destroy your own districts.");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid player or district number.");
                                }
                                break;
                            case "museum":
                                if (parts.length < 3) {
                                    System.out.println("Usage: action museum <card number>");
                                } else {
                                    try {
                                        int cardIndex = Integer.parseInt(parts[2]) - 1;
                                        storeCardUnderMuseum(currentPlayer, cardIndex);
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid card number.");
                                    }
                                }
                                break;
                            case "armory":
                                if (parts.length < 4) {
                                    System.out.println("Usage: action armory <player number> <district number>");
                                } else {
                                    try {
                                        int targetPlayerNum = parsePlayerNumber(parts[2]);
                                        if (targetPlayerNum != -1 && targetPlayerNum != currentPlayer.getPlayerNumber()) {
                                            Player targetPlayer = players.get(targetPlayerNum - 1);
                                            int districtIndex = Integer.parseInt(parts[3]) - 1;
                                            if (districtIndex >= 0 && districtIndex < targetPlayer.getCity().size()) {
                                                // Find and remove the Armory from current player's city
                                                boolean hasArmory = currentPlayer.getCity().removeIf(card -> card.getName().equals("Armory"));
                                                if (hasArmory) {
                                                    targetPlayer.getCity().remove(districtIndex);
                                                    System.out.println("Used Armory to destroy a district in Player " + targetPlayerNum + "'s city.");
                                                } else {
                                                    System.out.println("You don't have the Armory in your city.");
                                                }
                                            } else {
                                                System.out.println("Invalid district number.");
                                            }
                                        } else {
                                            System.out.println("Invalid player number or cannot destroy your own districts.");
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid player or district number.");
                                    }
                                }
                                break;
                            case "laboratory":
                                if (!currentPlayer.getCity().stream().anyMatch(c -> c.getName().equals("Laboratory"))) {
                                    System.out.println("You don't have the Laboratory.");
                                    break;
                                }
                                if (laboratoryUsed.getOrDefault(currentPlayer, false)) {
                                    System.out.println("You have already used Laboratory this turn.");
                                    break;
                                }
                                if (parts.length < 3) {
                                    System.out.println("Usage: action laboratory <card number>");
                                    break;
                                }
                                try {
                                    int cardIndex = Integer.parseInt(parts[2]) - 1;
                                    if (cardIndex >= 0 && cardIndex < currentPlayer.getHand().size()) {
                                        currentPlayer.getHand().remove(cardIndex);
                                        currentPlayer.addGold(1);
                                        laboratoryUsed.put(currentPlayer, true);
                                        System.out.println("Discarded a card for 1 gold (Laboratory).");
                                    } else {
                                        System.out.println("Invalid card number.");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid card number.");
                                }
                                break;
                            case "smithy":
                                if (!currentPlayer.getCity().stream().anyMatch(c -> c.getName().equals("Smithy"))) {
                                    System.out.println("You don't have the Smithy.");
                                    break;
                                }
                                if (smithyUsed.getOrDefault(currentPlayer, false)) {
                                    System.out.println("You have already used Smithy this turn.");
                                    break;
                                }
                                if (currentPlayer.getGold() < 2) {
                                    System.out.println("Not enough gold for Smithy (need 2).");
                                    break;
                                }
                                currentPlayer.addGold(-2);
                                for (int i = 0; i < 3; i++) {
                                    if (!districtDeck.isEmpty()) {
                                        currentPlayer.addToHand(districtDeck.remove(0));
                                    }
                                }
                                smithyUsed.put(currentPlayer, true);
                                System.out.println("Drew 3 cards for 2 gold (Smithy).");
                                break;
                            default:
                                System.out.println("Unknown action command. Available actions:");
                                showActionInfo(currentPlayer);
                        }
                    }
                } else {
                    System.out.println("You can only use actions during your turn.");
                }
                break;
            case "info":
                if (parts.length > 1) {
                    if (currentPlayer != null && currentPlayer.isHuman()) {
                        showInfo(currentPlayer, parts[1]);
                    } else {
                        System.out.println("You can only view info about your own cards.");
                    }
                } else {
                    System.out.println("Usage: info <H|name>");
                }
                break;
            case "all":
                showAllPlayers();
                break;
            case "save":
                if (parts.length > 1) {
                    try {
                        saveGame(parts[1]);
                    } catch (IOException e) {
                        System.err.println("Error saving game: " + e.getMessage());
                    }
                } else {
                    System.out.println("Usage: save <file>");
                }
                break;
            case "load":
                if (parts.length > 1) {
                    try {
                        loadGame(parts[1]);
                    } catch (IOException e) {
                        System.err.println("Error loading game: " + e.getMessage());
                    }
                } else {
                    System.out.println("Usage: load <file>");
                }
                break;
            case "end":
                if (currentPlayer != null && currentPlayer.isHuman()) {
                    System.out.println("You ended your turn.");
                    return;
                } else {
                    System.out.println("You can only end your own turn.");
                }
                break;
            case "help":
                showHelp();
                break;
            case "debug":
                debugMode = !debugMode;
                System.out.println("Debug mode is now " + (debugMode ? "ON" : "OFF"));
                break;
            case "t":
                if (currentPlayer != null && currentPlayer.isHuman()) {
                    System.out.println("Your turn.");
                } else {
                    System.out.println("It is not your turn.");
                }
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private int parsePlayerNumber(String s) {
        try {
            int p = Integer.parseInt(s);
            if (p >= 1 && p <= players.size()) return p;
        } catch (Exception e) {}
        System.out.println("Invalid player number.");
        return -1;
    }

    private void buildDistrictWithDuplicateCheck(Player player, int cardIndex) {
        try {
            if (cardIndex < 0 || cardIndex >= player.getHand().size()) {
                System.out.println("Invalid card number.");
                return;
            }

            DistrictCard card = player.getHand().get(cardIndex);
            if (card == null) {
                System.out.println("Invalid card (null).");
                return;
            }
            if (player.getGold() < card.getCost()) {
                System.out.println("Not enough gold to build this district.");
                return;
            }

            // Check for duplicates
            boolean hasDuplicate = player.getCity().stream()
                .anyMatch(c -> c.getName().equals(card.getName()));
            if (hasDuplicate) {
                System.out.println("You already have a " + card.getName() + " in your city.");
                return;
            }

            // Build the district
            player.addGold(-card.getCost());
            player.getHand().remove(cardIndex);
            player.getCity().add(card);
            System.out.println("Built " + card.getName() + " [" + card.getColor() + card.getCost() + "]");

            // Trigger purple card abilities
            triggerPurpleAbilityWhenBuilt(player, card);

            // Check for game end
            checkGameEnd();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Invalid build operation: " + e.getMessage());
        }
    }

    // Helper for storing a card under the Museum
    private void storeCardUnderMuseum(Player player, int cardIndex) {
        if (cardIndex >= 0 && cardIndex < player.getHand().size()) {
            DistrictCard storedCard = player.getHand().remove(cardIndex);
            museumStorage.computeIfAbsent(player, k -> new ArrayList<>()).add(storedCard);
            System.out.println("Stored " + storedCard.getName() + " under the Museum.");
        } else {
            System.out.println("Invalid card number.");
        }
    }

    // Update triggerPurpleAbilityWhenBuilt to use the helper
    private void triggerPurpleAbilityWhenBuilt(Player player, DistrictCard card) {
        if (card.getName().equals("Museum")) {
            if (player.getHand().isEmpty()) {
                System.out.println("No cards in hand to store under the Museum.");
                return;
            }
            System.out.println("Choose a card to store under the Museum (1-" + player.getHand().size() + "):");
            String input = scanner.nextLine();
            try {
                int cardIndex = Integer.parseInt(input) - 1;
                storeCardUnderMuseum(player, cardIndex);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. No card was stored.");
            }
        }
    }
    
    private void showActionInfo(Player player) {
        if (player == null) {
            System.out.println("No player selected.");
            return;
        }

        if (player.getCharacter() == null) {
            System.out.println("No character selected.");
            return;
        }

        String characterName = player.getCharacter().getName();
        System.out.println("Available actions for " + characterName + ":");
        
        // Character-specific actions
        switch (characterName) {
            case "Assassin":
                System.out.println("action kill <character#> - Kill a character (2-8)");
                break;
            case "Thief":
                System.out.println("action steal <character#> - Steal from a character (3-8)");
                break;
            case "Magician":
                System.out.println("action swap <player#> - Swap hands with another player");
                System.out.println("action redraw <card#> - Discard a card and draw a new one");
                break;
            case "Warlord":
                System.out.println("action destroy <player#> <district#> - Destroy a district in another player's city");
                break;
            default:
                System.out.println("No special character actions available.");
                break;
        }

        // Purple card actions
        boolean hasPurpleActions = false;
        for (DistrictCard card : player.getCity()) {
            if (card.getName().equals("Museum")) {
                System.out.println("action museum <card#> - Store a card under the Museum for end-game points");
                hasPurpleActions = true;
            } else if (card.getName().equals("Armory")) {
                System.out.println("action armory <player#> <district#> - Destroy the Armory and destroy a district in another player's city");
                hasPurpleActions = true;
            } else if (card.getName().equals("Laboratory")) {
                System.out.println("action laboratory <card#> - Discard a card to gain 1 gold");
                hasPurpleActions = true;
            } else if (card.getName().equals("Smithy")) {
                System.out.println("action smithy - Pay 2 gold to draw 3 cards");
                hasPurpleActions = true;
            }
        }

        if (!hasPurpleActions) {
            System.out.println("No special district actions available.");
        }
    }

    private void showInfo(Player player, String arg) {
        // Try to parse as hand index first
        try {
            int idx = Integer.parseInt(arg) - 1;
            List<DistrictCard> hand = player.getHand();
            if (idx >= 0 && idx < hand.size()) {
                DistrictCard card = hand.get(idx);
                if (card.getColor().equals("purple")) {
                    System.out.println("Info for " + card.getName() + ": " + card.getSpecialAbility());
                } else {
                    System.out.println("This card has no special ability.");
                }
                return;
            }
        } catch (Exception e) {}
        
        // Otherwise, treat as character name
        for (CharacterCard c : characterDeck) {
            if (c.getName().equalsIgnoreCase(arg)) {
                System.out.println("Info for " + c.getName() + ": " + c.getSpecialAbility());
                return;
            }
        }
        System.out.println("No info found for: " + arg);
    }

    /**
     * Saves the current game state to a file.
     *
     * @param filename The name of the file to save to
     * @throws IOException if there is an error writing to the file
     */
    public void saveGame(String filename) throws IOException {
        try {
            JSONObject gameState = new JSONObject();
            
            // Save basic game info
            gameState.put("currentRound", currentRound);
            gameState.put("gameEnded", gameEnded);
            gameState.put("debugMode", debugMode);
            
            // Save players
            JSONArray playersArray = new JSONArray();
            for (Player player : players) {
                JSONObject playerObj = new JSONObject();
                playerObj.put("playerNumber", player.getPlayerNumber());
                playerObj.put("isHuman", player.isHuman());
                playerObj.put("hasCrown", player.hasCrown());
                playerObj.put("gold", player.getGold());
                
                // Save character
                if (player.getCharacter() != null) {
                    JSONObject characterObj = new JSONObject();
                    characterObj.put("name", player.getCharacter().getName());
                    characterObj.put("number", player.getCharacter().getNumber());
                    characterObj.put("specialAbility", player.getCharacter().getSpecialAbility());
                    playerObj.put("character", characterObj);
                }
                
                // Save hand
                JSONArray handArray = new JSONArray();
                for (DistrictCard card : player.getHand()) {
                    JSONObject cardObj = new JSONObject();
                    cardObj.put("name", card.getName());
                    cardObj.put("color", card.getColor());
                    cardObj.put("cost", card.getCost());
                    cardObj.put("specialAbility", card.getSpecialAbility());
                    handArray.add(cardObj);
                }
                playerObj.put("hand", handArray);
                
                // Save city
                JSONArray cityArray = new JSONArray();
                for (DistrictCard card : player.getCity()) {
                    JSONObject cardObj = new JSONObject();
                    cardObj.put("name", card.getName());
                    cardObj.put("color", card.getColor());
                    cardObj.put("cost", card.getCost());
                    cardObj.put("specialAbility", card.getSpecialAbility());
                    cityArray.add(cardObj);
                }
                playerObj.put("city", cityArray);
                
                playersArray.add(playerObj);
            }
            gameState.put("players", playersArray);
            
            // Write to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(gameState.toJSONString());
            }
            
            System.out.println("Game saved successfully to " + filename);
            
        } catch (IOException e) {
            throw new IOException("Error saving game: " + e.getMessage());
        }
    }

    /**
     * Loads a game state from a file.
     *
     * @param filename The name of the file to load from
     * @throws IOException if there is an error reading from the file
     */
    public void loadGame(String filename) throws IOException {
        try {
            JSONParser parser = new JSONParser();
            JSONObject gameState;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                gameState = (JSONObject) parser.parse(reader);
            } catch (ParseException e) {
                throw new IOException("Error parsing save file: " + e.getMessage());
            }
            
            // Load basic game info
            currentRound = ((Long) gameState.get("currentRound")).intValue();
            gameEnded = (Boolean) gameState.get("gameEnded");
            debugMode = (Boolean) gameState.get("debugMode");
            
            // Clear existing game state
            players.clear();
            districtDeck.clear();
            characterDeck.clear();
            availableCharacters.clear();
            killedCharacter = null;
            robbedCharacter = null;
            
            // Load players
            JSONArray playersArray = (JSONArray) gameState.get("players");
            for (Object obj : playersArray) {
                JSONObject playerObj = (JSONObject) obj;
                Player player = new Player(
                    ((Long) playerObj.get("playerNumber")).intValue(),
                    (Boolean) playerObj.get("isHuman")
                );
                player.setHasCrown((Boolean) playerObj.get("hasCrown"));
                player.addGold(((Long) playerObj.get("gold")).intValue());
                
                // Load character
                if (playerObj.containsKey("character")) {
                    JSONObject characterObj = (JSONObject) playerObj.get("character");
                    CharacterCard character = new CharacterCard(
                        (String) characterObj.get("name"),
                        ((Long) characterObj.get("number")).intValue(),
                        (String) characterObj.get("specialAbility")
                    );
                    player.setCharacter(character);
                }
                
                // Load hand
                JSONArray handArray = (JSONArray) playerObj.get("hand");
                for (Object cardObj : handArray) {
                    JSONObject card = (JSONObject) cardObj;
                    DistrictCard districtCard = new DistrictCard(
                        (String) card.get("name"),
                        (String) card.get("color"),
                        ((Long) card.get("cost")).intValue(),
                        (String) card.get("specialAbility")
                    );
                    player.addToHand(districtCard);
                }
                
                // Load city
                JSONArray cityArray = (JSONArray) playerObj.get("city");
                for (Object cardObj : cityArray) {
                    JSONObject card = (JSONObject) cardObj;
                    DistrictCard districtCard = new DistrictCard(
                        (String) card.get("name"),
                        (String) card.get("color"),
                        ((Long) card.get("cost")).intValue(),
                        (String) card.get("specialAbility")
                    );
                    player.buildDistrict(districtCard);
                }
                
                players.add(player);
            }
            
            System.out.println("Game loaded successfully from " + filename);
            
        } catch (IOException e) {
            throw new IOException("Error loading game: " + e.getMessage());
        }
    }

    /**
     * Displays help information about available commands.
     */
    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("info <H|name> : show information about a character or building");
        System.out.println("t : processes turns");
        System.out.println("all : shows all current game info");
        System.out.println("citadel/list/city [p] : shows districts built by a player");
        System.out.println("hand : shows cards in hand");
        System.out.println("gold [p] : shows gold of a player");
        System.out.println("build <place in hand> : Builds a building into your city");
        System.out.println("action : Gives info about your special action and how to perform it");
        System.out.println("end : Ends your turn");
        System.out.println("save <file> : Save the game to a file");
        System.out.println("load <file> : Load the game from a file");
        System.out.println("debug : Toggles debug mode");
        System.out.println("help : show this help message");
    }

    /**
     * Displays the cards in a player's hand.
     *
     * @param player The player whose hand to display
     */
    private void showHand(Player player) {
        System.out.println("You have " + player.getGold() + " gold. Cards in hand:");
        List<DistrictCard> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            DistrictCard card = hand.get(i);
            System.out.println((i + 1) + ". " + card);
        }
    }

    /**
     * Displays the districts built in a player's city.
     *
     * @param player The player whose city to display
     */
    private void showCity(Player player) {
        System.out.println("Player " + player.getPlayerNumber() + " has built:");
        for (DistrictCard card : player.getCity()) {
            System.out.println(card);
        }
    }

    /**
     * Displays information about all players in the game.
     */
    private void showAllPlayers() {
        for (Player p : players) {
            System.out.print("Player " + p.getPlayerNumber());
            if (p.isHuman()) {
                System.out.print(" (you)");
            }
            System.out.print(": cards=" + p.getHand().size() + " gold=" + p.getGold() + " city=");
            for (DistrictCard card : p.getCity()) {
                System.out.print(card.getName() + " [" + card.getColor() + card.getCost() + "], ");
            }
            System.out.println();
        }
    }

    /**
     * Checks if the game has ended based on victory conditions.
     */
    public void checkGameEnd() {
        for (Player player : players) {
            if (player.getCity().size() >= 8) {
                gameEnded = true;
                break;
            }
        }
        if (gameEnded) {
            endGame();
        }
    }

    /**
     * Handles the end of the game, calculating and displaying final scores.
     */
    public void endGame() {
        System.out.println("\n================================");
        System.out.println("GAME ENDED");
        System.out.println("================================");
        
        // Calculate and display scores
        int maxScore = 0;
        Player winner = null;
        
        for (Player player : players) {
            int score = player.calculateScore();
            System.out.println("Player " + player.getPlayerNumber() + " score: " + score);
            
            if (score > maxScore) {
                maxScore = score;
                winner = player;
            }
        }
        
        if (winner != null) {
            System.out.println("\nPlayer " + winner.getPlayerNumber() + " wins!");
        } else {
            System.out.println("\nNo winner - game ended in a tie!");
        }

        // After calculating base score
        // Add purple card end-game bonuses
        for (Player player : players) {
            int bonus = 0;
            for (DistrictCard card : player.getCity()) {
                switch (card.getName()) {
                    case "Dragon Gate":
                    case "University":
                        bonus += 8 - card.getCost(); // Already counted cost, add extra
                        break;
                    case "Imperial Treasury":
                        bonus += player.getGold();
                        break;
                    case "Map Room":
                        bonus += player.getHand().size();
                        break;
                    case "Wishing Well":
                        long otherPurples = player.getCity().stream().filter(c -> c.getColor().equals("purple") && !c.getName().equals("Wishing Well")).count();
                        bonus += otherPurples;
                        break;
                    case "Museum":
                        if (museumStorage.containsKey(player)) {
                            bonus += museumStorage.get(player).size();
                        }
                        break;
                    // Add more as needed
                }
            }
            if (bonus > 0) {
                System.out.println("Player " + player.getPlayerNumber() + " received " + bonus + " bonus points from purple cards.");
            }
        }
    }

    // Restore missing AI and utility methods
    private void handleStartOfTurnAbilitiesAI(Player player) {
        String characterName = player.getCharacter().getName();
        if (characterName.equals("Assassin")) {
            int choice = 2 + (int)(Math.random() * 7); // 2-8
            String targetCharacter = getCharacterName(choice);
            killedCharacter = findCharacterCard(targetCharacter);
        }
        if (characterName.equals("Thief")) {
            int choice = 3 + (int)(Math.random() * 6); // 3-8
            String targetCharacter = getCharacterName(choice);
            robbedCharacter = findCharacterCard(targetCharacter);
        }
        // Handle district-based gold bonuses (only after first turn)
        if (currentRound > 1) {
            int bonusGold = 0;
            switch (characterName) {
                case "King":
                    bonusGold = countDistrictsByColor(player, "yellow");
                    break;
                case "Bishop":
                    bonusGold = countDistrictsByColor(player, "blue");
                    break;
                case "Merchant":
                    bonusGold = countDistrictsByColor(player, "green") + 1; // +1 for merchant bonus
                    break;
                case "Warlord":
                    bonusGold = countDistrictsByColor(player, "red");
                    break;
            }
            if (bonusGold > 0) {
                player.addGold(bonusGold);
            }
        }
    }

    private boolean shouldAITakeGold(Player player) {
        // AI logic: take gold if:
        // 1. Hand is empty or almost empty
        // 2. Has expensive cards in hand
        // 3. Has less than 3 gold and no cheap cards to build
        // 4. Random chance otherwise
        if (player.getHand().size() <= 1) {
            return true;
        }
        
        boolean hasExpensiveCard = player.getHand().stream().anyMatch(card -> card.getCost() > 3);
        if (hasExpensiveCard) {
            return true;
        }
        
        boolean hasCheapCard = player.getHand().stream().anyMatch(card -> card.getCost() <= player.getGold());
        if (!hasCheapCard && player.getGold() < 3) {
            return true;
        }
        
        return Math.random() < 0.4; // 40% chance to take gold otherwise
    }

    private void drawAndChooseCardAI(Player player) {
        List<DistrictCard> drawnCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            if (!districtDeck.isEmpty()) {
                drawnCards.add(districtDeck.remove(0));
            }
        }
        // AI logic: choose the most expensive card
        DistrictCard bestCard = null;
        int maxCost = -1;
        for (DistrictCard card : drawnCards) {
            if (card.getCost() > maxCost) {
                bestCard = card;
                maxCost = card.getCost();
            }
        }
        if (bestCard != null) {
            player.addToHand(bestCard);
            // Return other card to deck
            for (DistrictCard card : drawnCards) {
                if (card != bestCard) {
                    districtDeck.add(card);
                }
            }
            Collections.shuffle(districtDeck);
        }
    }

    private DistrictCard findBestCardToBuild(Player player) {
        DistrictCard bestCard = null;
        int maxCost = -1;
        for (DistrictCard card : player.getHand()) {
            if (card.getCost() <= player.getGold() && card.getCost() > maxCost) {
                bestCard = card;
                maxCost = card.getCost();
            }
        }
        return bestCard;
    }

    private boolean shouldAIUseSpecialAbility(Player player) {
        String characterName = player.getCharacter().getName();
        switch (characterName) {
            case "Magician":
                // Use if hand is empty or has bad cards
                return player.getHand().isEmpty() || player.getHand().stream().allMatch(card -> card.getCost() < 3);
            case "Architect":
                // Use if can build multiple districts
                return player.getGold() >= 6;
            case "Warlord":
                // Use if has enough gold to destroy and has red districts
                return player.getGold() >= 3 && countDistrictsByColor(player, "red") > 0;
            default:
                return false;
        }
    }

    private void useSpecialAbilityAI(Player player) {
        String characterName = player.getCharacter().getName();
        switch (characterName) {
            case "Magician":
                // TODO: Implement AI magician ability
                break;
            case "Architect":
                // Already handled in processAITurn
                break;
            case "Warlord":
                // TODO: Implement AI warlord ability
                break;
        }
    }

    private CharacterCard findCharacterCard(String name) {
        for (CharacterCard card : characterDeck) {
            if (card.getName().equals(name)) {
                return card;
            }
        }
        return null;
    }

    private int countDistrictsByColor(Player player, String color) {
        int count = (int) player.getCity().stream().filter(card -> card.getColor().equals(color)).count();
        // School of Magic: counts as any color for income
        boolean hasSchool = player.getCity().stream().anyMatch(c -> c.getName().equals("School Of Magic"));
        if (hasSchool) count++;
        return count;
    }

    /**
     * Main method to start the game.
     * Prompts for number of players and creates a new game instance.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numPlayers;
        
        System.out.print("Enter how many players [4-7]: ");
        numPlayers = scanner.nextInt();
        
        while (numPlayers < 4 || numPlayers > 7) {
            System.out.print("Please enter a number between 4 and 7: ");
            numPlayers = scanner.nextInt();
        }
        
        Game game = new Game(numPlayers);
        game.startGame();
    }

    /**
     * Returns the list of players in the game.
     *
     * @return List of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Returns the current round number.
     *
     * @return Current round number
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Returns whether the game has ended.
     *
     * @return true if the game has ended, false otherwise
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * Returns the character that was killed by the Assassin in the current round.
     *
     * @return The killed character, or null if no character was killed
     */
    public CharacterCard getKilledCharacter() {
        return killedCharacter;
    }

    /**
     * Returns the character that was robbed by the Thief in the current round.
     *
     * @return The robbed character, or null if no character was robbed
     */
    public CharacterCard getRobbedCharacter() {
        return robbedCharacter;
    }

    /**
     * Forces the game to end.
     * Used primarily for testing.
     */
    public void forceEndGame() {
        this.gameEnded = true;
    }

    /**
     * Triggers the start-of-turn character abilities for the given player (for testing).
     * This allows tests to simulate character abilities without a full turn.
     */
    public void triggerCharacterAbility(Player player) {
        // Temporarily set currentRound to 2 to allow bonuses in tests
        int oldRound = currentRound;
        currentRound = 2;
        if (player.isHuman()) {
            handleStartOfTurnAbilitiesHuman(player);
        } else {
            handleStartOfTurnAbilitiesAI(player);
        }
        currentRound = oldRound;
    }
} 