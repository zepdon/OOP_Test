package GameState;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private double currentBudget;
    private double interestRatePercentage;
    private List<Hex> boughtHexes;
    private List<Minion> minionsOwned;
    private int spawnsUsed;

    public Player(double initBudget, double interestRatePercentage) {
        this.currentBudget = initBudget;
        this.interestRatePercentage = interestRatePercentage;
        this.boughtHexes = new ArrayList<>();
        this.minionsOwned = new ArrayList<>();
        this.spawnsUsed = 0;
    }

    public double getCurrentBudget() {
        return currentBudget;
    }

    //budget but return as int
    public int budget() {
        return (int) currentBudget;
    }

    public void adjustBudget(double amount) {
        currentBudget += amount;
    }

    public double getInterestRatePercentage() {
        return interestRatePercentage;
    }

    public List<Hex> getBoughtHexes() {
        return boughtHexes;
    }

    //Hex cost in Config file
    public void buyHex(Hex hex, long cost) {
        if (!canBuyHex(hex)) {
            System.out.println("You can only buy a hex that is adjacent to one of your owned hexes.");
            return;
        }
        if (currentBudget >= cost) {
            adjustBudget(-cost);
            boughtHexes.add(hex);
            hex.setOwner(this);
            System.out.println("Bought hex at (" + hex.getRow() + ", " + hex.getCol() + ")");
        } else {
            System.out.println("Insufficient funds to buy hex at (" + hex.getRow() + ", " + hex.getCol() + ").");
        }
    }


    public boolean canBuyHex(Hex hex) {
        // If hex is already owned, you cannot buy it.
        if (hex.getOwner() != null) {
            return false;
        }
        // Check each hex that the player already owns.
        for (Hex ownedHex : boughtHexes) {
            // Each owned hex has a list of adjacent hexes.
            for (Hex adjacent : ownedHex.getAdjacentHexes()) {
                // If the target hex is found among the adjacent hexes, it's buyable.
                if (adjacent == hex) {
                    return true;
                }
            }
        }
        // If no adjacent hex is found, return false.
        return false;
    }


    public List<Minion> getMinionsOwned() {
        return minionsOwned;
    }

    public void addMinion(Minion minion) {
        minionsOwned.add(minion);
    }

    public int getSpawnsUsed() {
        return spawnsUsed;
    }

    public void incrementSpawnsUsed() {
        spawnsUsed++;
    }
}
