package players;
/*
 *  This class is a container for shared AI methods. Fx if all AI wants to figure something out, or uses helper methods, this is where they can be defined.
 *  They can also be overridden later, if need be.
 */

import containers.Move;
import containers.Reinforcement;
import game.Board;

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
    public abstract Reinforcement reinforce(Board board, int reinforceRemaining);


    /*
     *  All AI should evaluate a board
     *  It does depend on what the AI tailored to do, so it is abstract here
     */
    public abstract int evaluateBoard(Board board);

    /*
     *  Example actual AI method
     */
    public int moveOptions(){
        return 8;
    }
}