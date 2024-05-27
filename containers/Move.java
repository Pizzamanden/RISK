package containers;
import game.Land;
import players.Player;

/**
 * Move represents the action of moving troops.
 * A Move can either be a movement between connected friendly lands of "from",
 * or an attack to a neighbouring hostile Land of "from".
 * Count indicates how many troops is part of the Move action.
 */
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
