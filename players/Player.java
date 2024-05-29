package players;

import containers.*;
import game.*;

public abstract class Player{
    
    public int assignedNumber;

    public Player(int assignedNumber){
        this.assignedNumber = assignedNumber;
    }

    /*
     *  The abstract method any player of our game must implement
     *  If the returned move is not legal, the instance will have this method called again
     */
    public abstract Move move(Board board);

    /*
     *  The abstract method any player of our game must implement
     *  If the returned move is not legal, the instance will have this method called again
     */
    public abstract Move attack(Board board);

    /*
     *  The abstract method any player of our game must implement
     *  Returns the reinforcement that the player wants to do
     */
    public abstract Reinforcement reinforce(Board board, int reinforceRemaining);
}