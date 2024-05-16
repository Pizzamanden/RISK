package containers;
import game.Land;
import players.Player;

public class Move {
    public Player player;
    public Land from;
    public Land to;
    public int count;

    public Move(Player player, Land from, Land to, int count){
        this.player = player;
        this.from = from;
        this.to = to;
        this.count = count;
    }
}
