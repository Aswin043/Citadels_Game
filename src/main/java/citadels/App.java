package citadels;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Entry point for the Citadels game application.
 * Handles user interaction and starts the game.
 */
public class App {
	/** Scanner for reading user input */
	private Scanner scanner;

	/**
	 * Constructs a new App instance.
	 */
	public App() {
		this.scanner = new Scanner(System.in);
	}

	/**
	 * Main method to start the application.
	 * @param args Command line arguments (not used)
	 */
	public static void main(String[] args) {
		App app = new App();
		app.start();
	}

	/**
	 * Starts the game and handles initial setup.
	 */
	private void start() {
		System.out.println("Welcome to Citadels!");
		int numPlayers = getNumberOfPlayers();
		System.out.println("Shuffling deck...");
		System.out.println("Adding characters...");
		System.out.println("Dealing cards...");
		Game game = new Game(numPlayers);
		game.startGame();
	}

	/**
	 * Prompts the user for the number of players and validates input.
	 * @return The number of players (between 4 and 7)
	 */
	private int getNumberOfPlayers() {
		int numPlayers;
		do {
			System.out.print("Enter how many players [4-7]: ");
			while (!scanner.hasNextInt()) {
				System.out.println("Please enter a number between 4 and 7.");
				scanner.next();
			}
			numPlayers = scanner.nextInt();
		} while (numPlayers < 4 || numPlayers > 7);
		return numPlayers;
	}
}
