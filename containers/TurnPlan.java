package containers;

import game.*;
import java.util.*;

public class TurnPlan {
    
    private Stack<Action> plan;

    public TurnPlan(){
        plan = new Stack<>(); // Stack or queue?
    }

    public void addAction(Board board, Object action){
        plan.add(new Action(board, action));
    }

    public Action nextAction(){
        return plan.pop();
    }

    public boolean isPlanEmpty(){
        return plan.empty();
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
        public Object action;

        public Action(Board currentBoard, Object action){
            this.currentBoard = currentBoard;
            this.action = action;
        }

    }
}
