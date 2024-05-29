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
            planTurn(0);
            if(cTurnPlan.isPlanEmpty()){ // Check if we just want to end our turn now
                return move;
            }
            // This action should be good, since we just made it
            nextAction = cTurnPlan.nextAction();
            
        }
        // Either we are still on plan, or we just replanned.
        // In any case, we can move ahead with the nextAction
        return (Move) nextAction.action;
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
            planTurn(reinforceRemaining);
        }
        // Execute reinforcements from plan
        Action nextAction = cTurnPlan.nextAction();
        if(!(nextAction.action instanceof Reinforcement)){
            // Something went wrong!
        }
        // Just return the action as the reinforcement that it is
        return (Reinforcement) nextAction.action;
    }

    /*
     *  This method sets the current turn plan
     */
    private void planTurn(int reinforceRemaining){
        ArrayList<Board> 
        if(reinforceRemaining > 0){
            // We are in reinforcement phase
            // We need to figure out which zone we want to place how many troops in
            // This should be done by looking at all the options of placements, and seeing what we can get done with it.
        } else {
            // We are in movement phase. This is indicative of a plan failing.
            // Use the current board to figure out what should be done
        }
        // Do AI magik
    }

    /*
     *  By going over all enemy bordering land, give each a score.
     *  Highest score is equivalent to being easiest target
     */
    private Land findTarget(Board board){

    }
    
}