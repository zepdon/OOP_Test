package GameState;

public enum Direction {
    // Format: Game.Direction(evenDeltaRow, evenDeltaCol, oddDeltaRow, oddDeltaCol)
    UP(-1, 0, -1, 0),
    UPRIGHT(-1, 1, 0, 1),
    DOWNRIGHT(0, 1, 1, 1),
    DOWN(1, 0, 1, 0),
    DOWNLEFT(0, -1, 1, -1),
    UPLEFT(-1, -1, 0, -1)
;

    private final int evenDeltaRow;
    private final int evenDeltaCol;
    private final int oddDeltaRow;
    private final int oddDeltaCol;

    Direction(int evenDeltaRow, int evenDeltaCol, int oddDeltaRow, int oddDeltaCol) {
        this.evenDeltaRow = evenDeltaRow;
        this.evenDeltaCol = evenDeltaCol;
        this.oddDeltaRow = oddDeltaRow;
        this.oddDeltaCol = oddDeltaCol;
    }

    /**
     * Returns the row delta given the current column (to decide if we’re in an even or odd column).
     *
     * @param currentCol the column index of the current hex
     * @return the row offset for this direction
     */
    public int getDeltaRow(int currentCol) {
        if (currentCol % 2 == 0) {
            return evenDeltaRow;
        } else {
            return oddDeltaRow;
        }
    }

    /**
     * Returns the column delta.
     * (In many cases, the column offset is the same regardless of parity, but this method
     * allows you to adjust if needed.)
     *
     * @param currentCol the column index of the current hex (provided for symmetry)
     * @return the column offset for this direction
     */
    public int getDeltaCol(int currentCol) {
        if (currentCol % 2 == 0) {
            return evenDeltaCol;
        } else {
            return oddDeltaCol;
        }
    }
}
