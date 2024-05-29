package containers;

import game.*;
import java.util.*;

public class TurnPlan {
    
    private Queue<Action> plan;

    public TurnPlan(){
        plan = new LinkedList<>(); // Stack or queue?
    }

    public void addAction(Board board, Object action){
        plan.add(new Action(board, action));
    }

    public Action nextAction(){
        return plan.remove();
    }

    public boolean isPlanEmpty(){
        return plan.isEmpty();
    }

    public void wipe(){
        plan.clear();
    }

    /*
     *  Uses peek() to give a preview of what the next action is.
     *  TODO remove this, it should be useless, as when we spend all our reinforcements, we are automatically put into the next game phase by the game
     *  The next actions should then be, correctly, a move and not a reinforcement. Check for this in both move() and reinforce().
     */
    public Object checkNextAction(){
        return plan.peek().action;
    }


    public class Action{
        public Board currentBoard;
        public Object action; // Actions are either Reinforcements or Moves

        public Action(Board currentBoard, Object action){
            this.currentBoard = currentBoard;
            this.action = action;
        }

    }
}
