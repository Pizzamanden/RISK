package players;
/*
 *  An example AI.
 *  This AI is what is actually instantiated in the game.
 */

import containers.Move;
import containers.Reinforcement;
import game.Board;

public class CompAggressive extends AI{

    private int depth;

    public CompAggressive(int assignedNumber, int depth){
        super(assignedNumber);
        this.depth = depth;
    }

    @Override
    public Move move(Board board) {
        Move move = null;
        // Time to actually make an AI
        // Finding the best move to make, in some order in this turn, does involve making your own move method, or using a list of helper methods from AI.
        return move;
    }

    @Override
    public int evaluateBoard(Board board) {
        int eval = 1;
        // Time to actually make an AI
        // Since this AI is aggressive, it wants to use AI tools, helper methods, but it needs to declare its own final evaluation.
        return eval;
    }

    @Override
    public Reinforcement reinforce(Board board, int reinforceRemaining) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reinforce'");
    }
    
}