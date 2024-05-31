package containers;

import game.*;
import java.util.*;

public class TurnPlan {
    
    private Queue<Action> plan;

    /*
     *  Constructor for a TurnPlan
     */
    public TurnPlan(){
        plan = new LinkedList<>();
    }

    /*
     *  Method for adding an action to the plan
     */
    public void addAction(Board board, Object action){
        plan.add(new Action(board, action));
    }

    /*
     *  Method for removing and retrieving the next action
     */
    public Action nextAction(){
        return plan.remove();
    }

    /*
     *  Method for checking if the plan is empty
     */
    public boolean isPlanEmpty(){
        return plan.isEmpty();
    }

    /*
     *  Method for resetting the plan
     */
    public void wipe(){
        plan.clear();
    }

    /*
     *  Container class
     *  Simplified as having actions as objects, since usage should always know what is what.
     */
    public class Action{
        public Board currentBoard;
        public Object action; // Actions are either Reinforcements or Moves

        public Action(Board currentBoard, Object action){
            this.currentBoard = currentBoard;
            this.action = action;
        }

    }
}
