package GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KOMBATMain {
    public static void main(String[] args) {
        try {
            // Load configuration from file (adjust the path as needed)
            String configFilePath = "config.txt";
            ConfigLoader config = ConfigLoader.getInstance(configFilePath);
            Scanner scanner = new Scanner(System.in);

            // Prompt the user to choose the game mode.
            System.out.println("Choose game mode:");
            System.out.println("1. DUEL (Player vs Player)");
            System.out.println("2. SOLITAIRE (Player vs Bot)");
            System.out.println("3. AUTO (Bot vs Bot)");
            System.out.print("Enter your choice (1-3): ");
            int modeChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            GameMode gameMode;
            switch (modeChoice) {
                case 1:
                    gameMode = GameMode.DUEL;
                    break;
                case 2:
                    gameMode = GameMode.SOLITAIRE;
                    break;
                case 3:
                    gameMode = GameMode.AUTO;
                    break;
                default:
                    System.out.println("Invalid choice. Defaulting to DUEL mode.");
                    gameMode = GameMode.DUEL;
                    break;
            }

            // Prompt the user to choose the number of minion types.
            System.out.print("Enter the number of minion types (between 1 and 5): ");
            int numMinionTypes = scanner.nextInt();
            scanner.nextLine(); // consume newline
            if (numMinionTypes < 1 || numMinionTypes > 5) {
                System.out.println("Invalid number. Defaulting to 1 type.");
                numMinionTypes = 1;
            }

            // Create a list of minion types with fixed names.
            // The name for minion type i will be simply the string representation of i.
            List<MinionType> minionTypes = new ArrayList<>();
            for (int i = 1; i <= numMinionTypes; i++) {
                System.out.print("Enter defense factor for minion type " + i + ": ");
                int defenseFactor = scanner.nextInt();
                scanner.nextLine(); // consume newline
                // Use the fixed name (e.g., "1" for type 1)
                MinionType type = new MinionType(String.valueOf(i), defenseFactor);
                minionTypes.add(type);
            }

            System.out.println("Game mode selected: " + gameMode);
            System.out.println("Number of minion types: " + numMinionTypes);

            // Initialize and configure the game.
            Game.initializeGame(config, gameMode);
            Game game = Game.getInstance();
            // Pass the list of minion types to the game.
            game.setMinionTypes(minionTypes);

            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
