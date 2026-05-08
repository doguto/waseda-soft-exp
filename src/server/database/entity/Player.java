package src.server.database.entity;

public class Player {
    public final String name;
    public Role role;
    public boolean alive;

    public Player(String name) {
        this.name = name;
        this.alive = true;
    }
}
