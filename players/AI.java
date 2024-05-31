package players;
/*
 *  This class is a container for shared AI methods. Fx if all AI wants to figure something out, or uses helper methods, this is where they can be defined.
 *  They can also be overridden later, if need be.
 */

import containers.*;
import game.*;
import java.util.*;

public abstract class AI extends Player{


    public AI(int assignedNumber) {
        super(assignedNumber);
    }

    /*
     *  Keep this abstract in this extension
     */
    public abstract Move move(Board board);

    /*
     *  Keep this abstract in this extension
     */
    public abstract Move attack(Board board);

    /*
     *  Keep this abstract in this extension
     */
    public abstract Reinforcement reinforce(Board board, int reinforceRemaining);
}