package players;
import game.*;
import containers.*;

/**
 * An attempt to make an AI :)
 */
public class AIAttempt {
    /**
     * Methods required:
     *  Reinforce - handles the reinforcement phase. There should be no considerations here, all should be calculated. 
     *  Actions - returns a list of actions the AI can perform at the current state. 
     *  Results - retuns a list of potential results (states) from a given action.
     *  Evaluation - returns a value for a given board (how advantageous it is for the AI).
     * 
     * A state is an instance of the board. The state space is therefore all permutations of the board. 
     * The state space is therefore infinite, since we can make an infinite number of board (due to troop count not having an upper limit)
     * 
     * The goal state for the AI is any of the states where the AI controls all lands.
     * The AI cannot find a goal state in every search, so instead the goal of a search is to find the state with the 
     * best-case board for the AI.
     * The value of the board is determined by the evaluation function.
     * 
     * Actions is called with a board and will return a list of actions the AI can peform.
     * These actions can be:
     *   - Attack. 
     * 
     * The Results function is called with a board and an action.
     * All possible boards that can come from this actions is then returned in a set. 
     * 
     * The steps the AI takes in its turn can look like:
     *   - Reinforce - if there are reinforcements to place - calculated.
     *   - While(Not Done With Turn)
     *      - Move - calculated.
     *      - Attack - best action found using an AND-OR tree - board-evaluation * probability-of-board
     *      - If the best option is to not attack or no more attack can be made, stop loop.
     */


    /**
     * The AI calcualtes a series of Reinforcements it wishes to do 
     * on the given Board for the given Player. 
     * @param board - the Board.
     * @param player - the Player the AI places reinforcements for.
     */
    public static Reinforcement[] reinforce(Board board, Player player){
        return null;
    }

    /**
     * The AI finds the best Moves from the given Board and returns it.
     * Only the last Move in the list can be an attack move, all other 
     * Moves are movement Moves. 
     * The movements Moves are calculated based in the state of the given Board.
     * The attack move is found through the construction of an AND-OR tree.
     * @param board - the Board.
     * @param player - the Player the AI plays for.
     * @return an array of Moves the AI wishes to perform. 
     */
    public static Move[] move(Board board, Player player){
        return null;
    }

    /**
     * Returns a list of all actions the AI can perform from the given Board
     * for the given Player
     * @param board - the Board
     * @param player - the Player the AI plays for.
     * @return an array of Moves the AI can perform on the given Board for the given Player
     */
    private static Move[] actions(Board board, Player player){
        return null;
    }

    /**
     * Applies the given Move on the given Board and returns an array
     * with all the possible results from performing the Action of the Board.
     * @param board - the Board.
     * @param action - the action to perform on the Board.
     * @return an array of Boards representing all results from performing the given Action on the given Board.
     */
    private static Board[] results(Board board, Move action){
        return null;
    }

    /**
     * Evaluates the given Board using some evaluation function.
     * The function determines how advantageous the Board currently 
     * is for the given Player.
     * @param board - the Board to evaluate
     * @return an evaluation of the gven Board from the given Players perspective
     */
    private static int evaluate(Board board, Player player){
        return 0;
    }
}
