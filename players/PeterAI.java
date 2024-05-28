package players;
/*
 *  An example AI.
 *  This AI is what is actually instantiated in the game.
 */

import containers.*;
import containers.TurnPlan.Action;
import game.*;

public class PeterAI extends AI{

    private int depth;
    private TurnPlan cTurnPlan;


    public PeterAI(int assignedNumber, int depth){
        super(assignedNumber);
        this.depth = depth;
        this.cTurnPlan = new TurnPlan();
    }

    @Override
    public Move move(Board board) {
        Move move = null;
        if(cTurnPlan.isPlanEmpty()){
            // If at this point the plan is empty, that correlates to ending our turn
            return move;
        }
        // If the plan is not empty, check that the next move has a board that matches the current board
        Action nextAction = cTurnPlan.nextAction();
        if(!(nextAction.action instanceof Move)){
            // Something went wrong
        }
        if(!(nextAction.currentBoard.equals(board))){
            // Our last move made has fucked up the timeline, replan!
            planTurn();
        }


        return move;
    }
 
    /*
     *  Implementation forced by AI class
     */
    @Override
    public int evaluateBoard(Board board) {
        int eval = 1;
        // Time to actually make an AI
        // Since this AI is aggressive, it wants to use AI tools, helper methods, but it needs to declare its own final evaluation.
        return eval;
    }

    /*
     *  For this AI, being asked to reinforce constitutes planning the entire turn
     */
    @Override
    public Reinforcement reinforce(Board board, int reinforceRemaining) {
        // If this is our first reinforcement, the TurnPlan should be empty
        if(cTurnPlan.isPlanEmpty()){
            planTurn();
        }

    }

    /*
     *  This method sets the current turn plan
     */
    private void planTurn(){
        // Do AI magik
    }
    
}