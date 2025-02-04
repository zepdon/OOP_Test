package GameLogic;

public interface MinionInterface {
    void done();
    void move(Direction direction);
    void shoot(Direction direction, int expenditure);
    int opponent();  // Returns info about the opponent (stub)
    int ally();      // Returns info about the ally (stub)
    int nearby(Direction direction); // Returns nearby information (stub)
    void checkCurrentHex();
}
