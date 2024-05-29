package players;
/*
 *  An example AI.
 *  This AI is what is actually instantiated in the game.
 */

import containers.*;
import containers.TurnPlan.Action;
import game.*;
import java.util.*;

public class SSAI extends AI{

    private int depth;
    private TurnPlan cTurnPlan;


    public SSAI(int assignedNumber, int depth){
        super(assignedNumber);
        this.depth = depth;
        this.cTurnPlan = new TurnPlan();
    }

    @Override
    public Move attack(Board board) {
        Action nextAction = cTurnPlan.nextAction();
        // If the plan is not empty, we might still be done with the attack phase
        if(!(nextAction.currentBoard.equals(board))){
            // Our last attack made has fucked up the timeline, replan!
            planTurn(board, 0);
            // This action should be good, since we just made it on the current board
            nextAction = cTurnPlan.nextAction();
        }
        // Either we are still on plan, or we just replanned.
        // In any case, we can move ahead with the nextAction
        return (Move) nextAction.action;
    }

    @Override
    public Move move(Board board) {
        Action nextAction = cTurnPlan.nextAction();
        // If the plan is not empty, we might still be done with the attack phase
        if(!(nextAction.currentBoard.equals(board))){
            // Our last move made has fucked up the timeline, replan!
            planTurn(board, 0);
            // This action should be good, since we just made it on the current board
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
        System.out.println("AI is asked for a reinforcement.");
        if(cTurnPlan.isPlanEmpty()){
            // We only replan here if we havent planned before. We either call planTurn with the reinforcements we had in the beginning of our turn, or with 0
            planTurn(board, reinforceRemaining);
        }
        // Execute reinforcements from plan
        Action nextAction = cTurnPlan.nextAction();
        if(!(nextAction.action instanceof Reinforcement)){
            // Something went wrong!
        }
        // Just return the action as the reinforcement that it is
        // No need to check for board equality, since this bears no risk of failure
        return (Reinforcement) nextAction.action;
    }


    /*
     *  This method sets the current turn plan
     *  This should be called if either:
     *  - There is no current plan
     *  - The plan that exists is no longer possible on the current board
     */
    private void planTurn(Board board, int reinforcementsToPlace){
        cTurnPlan.wipe();
        Board planBoard = board.copy();
        if(reinforcementsToPlace > 0){
            int reinforceRemaining = reinforcementsToPlace;
            // First we must reinforce
            // A better AI would plan its entire turn, and reinforce using that plan.
            // This AI just chooses weak border lands and adds troops on them
            ArrayList<Land> borderLands = planBoard.getControlledBorderLands(this);
            ArrayList<Reinforcement> reinforcementsToMake = new ArrayList<>();
            // For each land, we have a possible reinforcement to make
            for (Land land : borderLands) {
                reinforcementsToMake.add(new Reinforcement(land, 0));
            }
            while (reinforceRemaining > 0) {
                // Sort the lands, such that the ones with the lowest amount is first in the list
                borderLands.sort((l1, l2) -> Integer.compare(l1.getTroopCount(), l2.getTroopCount()));
                // Now we add to all the lands which share the lowest troop count
                // Or until we run out of reinforcements to place
                int landIndex = 0;
                int leastTroopsNum = borderLands.get(0).getTroopCount();
                // This loop runs as long as:
                // We still have reinforcements
                // We are still in bounds of the array
                // The land we are looking at are part of the lands which are tied for the lowest count
                while (landIndex < borderLands.size() && borderLands.get(landIndex).getTroopCount() == leastTroopsNum && reinforceRemaining > 0) {
                    // Find the reinforcement, and increase the troops we want to reinforce by 1
                    boolean found = false;
                    int reinfIndex = 0;
                    while(!found){
                        if(reinforcementsToMake.get(reinfIndex).land.equals(borderLands.get(landIndex))){
                            found = true;
                            reinforcementsToMake.get(reinfIndex).count = reinforcementsToMake.get(reinfIndex).count + 1;
                        }
                        reinfIndex++;
                    }
                    borderLands.get(landIndex).changeTroopCount(1);
                    // Also specify that we have one less reinforcement to place
                    reinforceRemaining--;
                    landIndex++;
                }
            }
            // For all these reinforcements, add them to the plan
            // But, our reinforcements use fake lands. So we need the actual lands
            for (Reinforcement reinf : reinforcementsToMake) {
                if(reinf.count > 0){
                    // Do not worry about the board being different
                    System.out.println("Adding reinforcement plan of " + reinf.land.getName() + " with " + reinf.count + " troops.");
                    reinf.land = board.getLandByName(reinf.land.getName());
                    // Add this reinforcement to the plan
                    cTurnPlan.addAction(board, reinf);
                }
            }
            // We are done fortifying the wall
        }
        // Now we plan our moves
        // Just return nonsense, and force the AI to rerun this method with the new actual board
        cTurnPlan.addAction(board, null);
        cTurnPlan.addAction(board, null);
    }
    
}