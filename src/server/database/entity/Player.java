package src.server.database.entity;

public class Player {
    public final String id;
    public final String name;
    public Role role;
    public boolean alive;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.alive = true;
    }
}
