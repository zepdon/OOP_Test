package GameState;

import java.util.List;

public class Board {
    private int rows = 9;
    private int cols = 9;
    private Hex[][] grid;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Hex[rows][cols];
        initializeGrid();
        setupAdjacentHexes();
    }

    private void initializeGrid() {
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                grid[r][c] = new Hex(r, c);
            }
        }
    }

    private void setupAdjacentHexes() {
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                Hex hex = grid[r][c];
                for (Direction d : Direction.values()) {
                    int nr = r + d.getDeltaRow(c);
                    int nc = c + d.getDeltaCol(c);
                    if (isWithinBounds(nr, nc)) {
                        hex.addAdjacentHex(grid[nr][nc]);
                    }
                }
            }
        }
    }

    public boolean isWithinBounds(int row, int col) {
        return (row > 0 && row < rows && col > 0 && col < cols);
    }

    public Hex getHex(int row, int col) {
        if (isWithinBounds(row, col)) {
            return grid[row][col];
        }
        return null;
    }

    /**
     * Prints the board in a staggered layout.
     * For each hex:
     *   - If unoccupied, prints "."
     *   - If occupied, prints "1" if the minion belongs to Player 1,
     *     "2" if it belongs to Player 2.
     */
    public void printBoard() {
        // Retrieve the players from the Game singleton.
        List<Player> players = Game.getInstance().getPlayers();

        // For an 8x8 grid.
        for (int r = 1; r < rows; r++) {
            // Indent odd-numbered rows to create a staggered effect.
            for (int c = 1; c < cols; c++) {
                Hex hex = grid[r][c];
                if (hex.isOccupied()) {
                    // If the hex is occupied, show which player's minion is there.
                    if (hex.getOccupant().getOwner() == players.get(0)) {
                        System.out.print(" 1 ");
                    } else if (players.size() > 1 && hex.getOccupant().getOwner() == players.get(1)) {
                        System.out.print(" 2 ");
                    } else {
                        System.out.print(" ? ");
                    }
                } else if (hex.getOwner() != null) {
                    // If the hex is unoccupied but belongs to a player (spawn zone), print a marker.
                    if (hex.getOwner() == players.get(0)) {
                        System.out.print("1S");
                    } else if (players.size() > 1 && hex.getOwner() == players.get(1)) {
                        System.out.print("2S");
                    } else {
                        System.out.print(" S");
                    }
                } else {
                    // Otherwise, print an empty cell.
                    System.out.print(" . ");
                }
                // Add spacing between columns.
                System.out.print("  ");
            }
            System.out.println();
        }
    }
}
