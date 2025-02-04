package GameLogic;
//add Strategy
public class Minion implements MinionInterface {
    protected int hp;
    protected int defenseFactor;
    protected int order; // Spawn order
    protected Hex currentHex;
    protected Player owner;

    public Minion(int hp, int defenseFactor, Hex spawnHex, Player owner, int order) {
        this.hp = hp;
        this.defenseFactor = defenseFactor;
        this.currentHex = spawnHex;
        this.owner = owner;
        this.order = order;
        // Place this minion on the board by marking the hex’s occupant.
        spawnHex.setOccupant(this);
    }

    public Player getOwner() {return owner;}


    @Override
    public void done() {
        // End of this minion's action sequence.
        System.out.println("Game.Minion " + order + " finished its actions.");
    }

    @Override
    public void move(Direction direction) {
        Hex targetHex = getTargetHex(direction);
        if (targetHex != null && !targetHex.isOccupied()) {
            // Check if the owner has enough budget (move cost is 1 unit)
            if (owner.getCurrentBudget() >= 1) {
                owner.adjustBudget(-1);
                // Update the board: remove from current hex and occupy target hex.
                currentHex.setOccupant(null);
                currentHex = targetHex;
                targetHex.setOccupant(this);
                System.out.println("Minion " + order + " moved to (" +
                        currentHex.getRow() + ", " + currentHex.getCol() + ")");
            } else {
                System.out.println("Insufficient budget to move.");
            }
        } else {
            System.out.println("Invalid move: target hex is either occupied or out of bounds.");
        }
    }

    @Override
    public void shoot(Direction direction, int expenditure) {
        if (owner.getCurrentBudget() >= expenditure + 1) {
            owner.adjustBudget(-(expenditure + 1));
            Hex targetHex = getTargetHex(direction);
            if (targetHex != null && targetHex.isOccupied()) {
                Minion targetMinion = targetHex.getOccupant();
                // Calculate damage: damage = max(1, expenditure - target's defenseFactor)
                int damage = Math.max(1, expenditure - targetMinion.defenseFactor);
                targetMinion.takeDamage(damage);
                System.out.println("Minion " + order + " shot target at (" +
                        targetHex.getRow() + ", " + targetHex.getCol() + ") for " +
                        damage + " damage.");
            } else {
                System.out.println("Shot missed: no minion at target hex.");
            }
        } else {
            System.out.println("Insufficient budget to shoot.");
        }
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
        if (hp == 0) {
            System.out.println("Minion at (" + currentHex.getRow() + ", " + currentHex.getCol() + ") has died.");
            currentHex.setOccupant(null);
            // Remove this minion from its owner's list.
            owner.getMinionsOwned().remove(this);
        }
    }

    @Override
    public int opponent() {
        int bestDistance = Integer.MAX_VALUE;
        int bestDirectionNumber = Integer.MAX_VALUE;  // used to break ties
        // Loop over all possible directions.
        for (Direction d : Direction.values()) {
            int candidateDistance = -1;  // distance where an opponent is found in direction d
            int distance = 1;
            // Scan outward in direction d.
            while (true) {
                Hex hex = getHexInDirection(d, distance);
                if (hex == null) {
                    // Went off the board, no minion found in this direction.
                    break;
                }
                if (hex.isOccupied()) {
                    Minion m = hex.getOccupant();
                    // Check if the minion belongs to the opponent.
                    if (!m.owner.equals(this.owner)) {
                        candidateDistance = distance;
                    }
                    // Stop scanning further in this direction whether it's an ally or opponent.
                    break;
                }
                distance++;
            }
            if (candidateDistance != -1) {
                // Determine the direction number (we use ordinal + 1 to get a value from 1 to 6).
                int candidateDirectionNumber = d.ordinal() + 1;
                // Select the candidate if it's closer than what we've found before,
                // or if equal distance but with a lower direction number.
                if (candidateDistance < bestDistance ||
                        (candidateDistance == bestDistance && candidateDirectionNumber < bestDirectionNumber)) {
                    bestDistance = candidateDistance;
                    bestDirectionNumber = candidateDirectionNumber;
                }
            }
        }
        // If no candidate was found, return 0.
        if (bestDistance == Integer.MAX_VALUE) {
            return 0;
        }
        // Encode the result: (distance * 10) + direction number.
        return bestDistance * 10 + bestDirectionNumber;
    }

    @Override
    public int ally() {
        int bestDistance = Integer.MAX_VALUE;
        int bestDirectionNumber = Integer.MAX_VALUE;  // used to break ties
        // Loop over all possible directions.
        for (Direction d : Direction.values()) {
            int candidateDistance = -1;  // distance where an opponent is found in direction d
            int distance = 1;
            // Scan outward in direction d.
            while (true) {
                Hex hex = getHexInDirection(d, distance);
                if (hex == null) {
                    // Went off the board, no minion found in this direction.
                    break;
                }
                if (hex.isOccupied()) {
                    Minion m = hex.getOccupant();
                    // Check if the minion belongs to the opponent.
                    if (m.owner.equals(this.owner)) {
                        candidateDistance = distance;
                    }
                    // Stop scanning further in this direction whether it's an ally or opponent.
                    break;
                }
                distance++;
            }
            if (candidateDistance != -1) {
                // Determine the direction number (we use ordinal + 1 to get a value from 1 to 6).
                int candidateDirectionNumber = d.ordinal() + 1;
                // Select the candidate if it's closer than what we've found before,
                // or if equal distance but with a lower direction number.
                if (candidateDistance < bestDistance ||
                        (candidateDistance == bestDistance && candidateDirectionNumber < bestDirectionNumber)) {
                    bestDistance = candidateDistance;
                    bestDirectionNumber = candidateDirectionNumber;
                }
            }
        }
        // If no candidate was found, return 0.
        if (bestDistance == Integer.MAX_VALUE) {
            return 0;
        }
        // Encode the result: (distance * 10) + direction number.
        return bestDistance * 10 + bestDirectionNumber;
    }

    /**
     * Implementation of the nearby function.
     * It looks for a minion in the specified direction.
     * Returns:
     *   0 if no minion is found,
     *   Otherwise, returns a value = 100*x + 10*y + z, where:
     *      x = number of digits in the target minion's HP,
     *      y = number of digits in the target minion's defense factor,
     *      z = distance (in hexes) to the target.
     * The value is negative if the target is an ally.
     */
    @Override
    public int nearby(Direction direction) {
        int distance = 1;
        while (true) {
            Hex targetHex = getHexInDirection(direction, distance);
            if (targetHex == null) {
                // Went off the board.
                return 0;
            }
            if (targetHex.isOccupied()) {
                Minion found = targetHex.getOccupant();
                int hpDigits = numberOfDigits(found.hp);
                int defDigits = numberOfDigits(found.defenseFactor);
                int result = 100 * hpDigits + 10 * defDigits + distance;
                // If the found minion belongs to the same player, return a negative value.
                if (found.owner.equals(this.owner)) {
                    return -result;
                } else {
                    return result;
                }
            }
            distance++;
        }
    }

    /**
     * Helper method that returns the hex at a given distance in the specified direction.
     * It iteratively moves one step at a time, recalculating offsets based on the current hex's column.
     */
    protected Hex getHexInDirection(Direction direction, int distance) {
        Hex hex = currentHex;
        for (int i = 0; i < distance; i++) {
            int currentCol = hex.getCol();
            int newRow = hex.getRow() + direction.getDeltaRow(currentCol);
            int newCol = hex.getCol() + direction.getDeltaCol(currentCol);
            Board board = Game.getInstance().getBoard();
            if (!board.isWithinBounds(newRow, newCol)) {
                return null;
            }
            hex = board.getHex(newRow, newCol);
        }
        return hex;
    }

    /**
     * Helper method to count the number of digits in a positive integer.
     */
    private int numberOfDigits(int value) {
        if (value == 0) return 1;
        int digits = 0;
        while (value > 0) {
            digits++;
            value /= 10;
        }
        return digits;
    }

    @Override
    public void checkCurrentHex() {
        System.out.println("Minion " + order + " is at (" +
                currentHex.getRow() + ", " + currentHex.getCol() + ")");
    }

    /**
     * A helper method that returns the hex immediately adjacent in the given direction (distance = 1).
     */
    protected Hex getTargetHex(Direction direction) {
        return getHexInDirection(direction, 1);
    }


}
