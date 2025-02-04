package GameLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    private static Game instance; // Singleton instance
    private Board board;
    private isRunning isRunning;
    private GameMode gameMode;
    private int currentTurn;
    private int maxTurns;
    private List<Player> players;
    private ConfigLoader config;
    private Scanner scanner;
    // List of available minion types; must be set before the game starts.
    private List<MinionType> minionTypes;

    // Private constructor for the singleton.
    // Note: The board is created as a 9x9 grid.
    private Game(ConfigLoader config, GameMode gameMode) {
        this.config = config;
        this.board = new Board(9, 9);
        this.isRunning = isRunning.RUNNING;
        this.currentTurn = 1;
        this.maxTurns = (int) config.maxTurns;
        this.players = new ArrayList<>();
        this.gameMode = gameMode;
        this.scanner = new Scanner(System.in);

        // For DUEL mode, create two human players.
        if (gameMode == GameMode.DUEL) {
            Player player1 = new Player(config.initBudget, config.interestPct);
            Player player2 = new Player(config.initBudget, config.interestPct);
            players.add(player1);
            players.add(player2);

            // Assign spawn zones for Player 1:
            board.getHex(1, 1).setOwner(player1);
            board.getHex(1, 2).setOwner(player1);
            board.getHex(1, 3).setOwner(player1);
            board.getHex(2, 1).setOwner(player1);
            board.getHex(2, 2).setOwner(player1);
            player1.getBoughtHexes().add(board.getHex(1, 1));
            player1.getBoughtHexes().add(board.getHex(1, 2));
            player1.getBoughtHexes().add(board.getHex(1, 3));
            player1.getBoughtHexes().add(board.getHex(2, 1));
            player1.getBoughtHexes().add(board.getHex(2, 2));

            // Assign spawn zones for Player 2:
            board.getHex(7, 7).setOwner(player2);
            board.getHex(7, 8).setOwner(player2);
            board.getHex(8, 6).setOwner(player2);
            board.getHex(8, 7).setOwner(player2);
            board.getHex(8, 8).setOwner(player2);
            player2.getBoughtHexes().add(board.getHex(7, 7));
            player2.getBoughtHexes().add(board.getHex(7, 8));
            player2.getBoughtHexes().add(board.getHex(8, 6));
            player2.getBoughtHexes().add(board.getHex(8, 7));
            player2.getBoughtHexes().add(board.getHex(8, 8));
        } else {
            throw new UnsupportedOperationException("Only DUEL mode is implemented.");
        }
    }

    // Setter for minionTypes; call this from your startup code after reading input.
    public void setMinionTypes(List<MinionType> minionTypes) {
        this.minionTypes = minionTypes;
    }

    // Returns the list of players.
    public List<Player> getPlayers() {
        return players;
    }

    // Initialize the game singleton.
    public static void initializeGame(ConfigLoader config, GameMode gameMode) {
        if (instance == null) {
            instance = new Game(config, gameMode);
        }
    }

    // Get the singleton instance.
    public static Game getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Game instance not created. Call initializeGame() first.");
        }
        return instance;
    }

    // Return the game board.
    public Board getBoard() {
        return board;
    }

    // The main game loop.
    public void start() {
        while (isRunning == isRunning.RUNNING && currentTurn <= maxTurns) {
            // Alternate turns between the players.
            for (Player player : players) {
                if (!checkEndGame()) {
                    System.out.println("=== Turn " + currentTurn + " ===");
                    performPlayerTurn(player);
                }else {
                    isRunning = isRunning.ENDED;
                    break;
                }
                currentTurn++;
            }
        }
        System.out.println("Game Over!");
        // Optionally announce the winner or final scores.
    }

    /**
     * Performs a complete turn for a human player.
     */
    private void performPlayerTurn(Player player) {
        // Reset the spawn counter at the beginning of each player's turn.

        System.out.println("Player " + (players.indexOf(player) + 1) + "'s turn:");
        System.out.println("Current budget: " + player.getCurrentBudget());
        board.printBoard();

        // --- Hex Purchase ---
        System.out.println("Do you want to purchase a hex? (y/n)");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("y")) {
            System.out.println("Enter row and column to purchase (e.g., '2 3'):");
            int row = scanner.nextInt();
            int col = scanner.nextInt();
            scanner.nextLine(); // consume newline
            Hex hex = board.getHex(row, col);
            if (hex != null && hex.getOwner() == null) {
                player.buyHex(hex, config.hexPurchaseCost);
            } else {
                System.out.println("Invalid hex purchase. Either out of bounds or already owned.");
            }
        }

        // --- Minion Spawn ---
        if (player.getSpawnsUsed() < config.maxSpawns) {
            System.out.println("Do you want to spawn a new minion? (y/n)");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("y")) {
                System.out.println("Enter row and column for spawn (e.g., '2 3'):");
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                scanner.nextLine(); // consume newline
                Hex spawnHex = board.getHex(row, col);
                if (spawnHex != null && !spawnHex.isOccupied() && spawnHex.getOwner() == player) {
                    if (player.getCurrentBudget() >= config.spawnCost) {
                        player.adjustBudget(-config.spawnCost);
                        // Ask the player to choose a minion type.
                        System.out.println("Choose a minion type:");
                        for (int i = 0; i < minionTypes.size(); i++) {
                            MinionType type = minionTypes.get(i);
                            System.out.println((i + 1) + ". " + type.getName() + " (Defense Factor: " + type.getDefenseFactor() + ")");
                        }
                        int typeChoice = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        if (typeChoice < 1 || typeChoice > minionTypes.size()) {
                            System.out.println("Invalid type, defaulting to type 1.");
                            typeChoice = 1;
                        }
                        MinionType chosenType = minionTypes.get(typeChoice - 1);
                        int spawnOrder = player.getMinionsOwned().size() + 1;
                        // Use the chosen type's defense factor.
                        Minion newMinion = new Minion((int) config.initHP, chosenType.getDefenseFactor(), spawnHex, player, spawnOrder);
                        player.addMinion(newMinion);
                        player.incrementSpawnsUsed();
                        board.printBoard();
                        System.out.println("Minion spawned at (" + spawnHex.getRow() + ", " + spawnHex.getCol() + ") with type: " + chosenType.getName());
                    } else {
                        System.out.println("Insufficient budget to spawn minion.");
                    }
                } else {
                    System.out.println("Invalid spawn hex. It might be occupied, out of bounds, or not your spawn zone.");
                }
            }
        }

        // --- Minion Actions ---
        for (Minion minion : player.getMinionsOwned()) {
            System.out.println("Minion " + minion.order + " is at (" +
                    minion.currentHex.getRow() + ", " + minion.currentHex.getCol() + ").");
            boolean turnEnded = false;
            while (!turnEnded) {
                System.out.println("Enter command for minion " + minion.order +
                        " (move, shoot, nearby, ally, opponent, or done):");
                System.out.println("  For move: 'move DIRECTION' (e.g., move UP, move downleft)");
                System.out.println("  For shoot: 'shoot DIRECTION EXPENDITURE' (e.g., shoot upright 10)");
                System.out.println("  For nearby: 'nearby DIRECTION' (e.g., nearby DOWN)");
                System.out.println("  For ally: type 'ally'");
                System.out.println("  For opponent: type 'opponent'");
                System.out.println("  Type 'done' to finish this minion's turn.");
                input = scanner.nextLine().trim();
                String[] tokens = input.split("\\s+");

                if (tokens.length == 0) {
                    System.out.println("No command entered, try again.");
                    continue;
                }

                String command = tokens[0].toLowerCase();
                switch (command) {
                    case "move":
                        if (tokens.length >= 2) {
                            try {
                                Direction dir = Direction.valueOf(tokens[1]);
                                minion.move(dir);
                                board.printBoard();
                            } catch (IllegalArgumentException e) {
                                System.out.println("Invalid direction. Try again.");
                            }
                        } else {
                            System.out.println("Please provide a direction for move.");
                        }
                        break;

                    case "shoot":
                        if (tokens.length >= 3) {
                            try {
                                Direction dir = Direction.valueOf(tokens[1]);
                                int expenditure = Integer.parseInt(tokens[2]);
                                minion.shoot(dir, expenditure);
                                board.printBoard();
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid expenditure value. Please enter a number.");
                            } catch (IllegalArgumentException e) {
                                System.out.println("Invalid direction. Try again.");
                            }
                        } else {
                            System.out.println("Please provide a direction and expenditure for shoot.");
                        }
                        break;

                    case "nearby":
                        if (tokens.length >= 2) {
                            try {
                                Direction dir = Direction.valueOf(tokens[1]);
                                long info = minion.nearby(dir);
                                System.out.println("Nearby(" + dir + ") returns: " + info);
                                board.printBoard();
                            } catch (IllegalArgumentException e) {
                                System.out.println("Invalid direction for nearby. Try again.");
                            }
                        } else {
                            System.out.println("Please provide a direction for nearby.");
                        }
                        break;

                    case "ally":
                        long allyInfo = minion.ally();
                        board.printBoard();
                        System.out.println("Ally() returns: " + allyInfo);

                        break;

                    case "opponent":
                        long opponentInfo = minion.opponent();
                        board.printBoard();
                        System.out.println("Opponent() returns: " + opponentInfo);
                        break;

                    case "done":
                        minion.done();
                        board.printBoard();
                        turnEnded = true;
                        break;

                    default:
                        System.out.println("Invalid command. Please try again.");
                        break;
                }
            }
        }
    }

    /**
     * Checks end-game conditions.
     * In this simple version, the game ends if any player has no minions (after turn 2).
     */
    private boolean checkEndGame() {
        for (Player player : players) {
            if (player.getMinionsOwned().isEmpty() && currentTurn > 2) {
                System.out.println("Player " + (players.indexOf(player) + 1) + " has no minions left.");
                return true;
            }
        }
        return false;
    }
}
