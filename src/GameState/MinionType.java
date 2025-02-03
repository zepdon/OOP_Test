package GameState;

public class MinionType {
    private String name;
    private int defenseFactor;

    public MinionType(String name, int defenseFactor) {
        this.name = name;
        this.defenseFactor = defenseFactor;
    }

    public String getName() {
        return name;
    }

    public int getDefenseFactor() {
        return defenseFactor;
    }
}
