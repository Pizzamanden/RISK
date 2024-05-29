package players;
import game.*;
import containers.*;
import java.util.ArrayList;

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
     * Actions is called with a board and will return a list of attacks the AI can peform.
     * 
     * The Results function is called with a board and an attack Move.
     * All possible boards that can come from this attack Move is then returned. 
     * 
     * The steps the AI takes in its turn can look like:
     *   - Reinforce - if there are reinforcements to place - calculated.
     *   - While(Not Done With Turn)
     *      - Attack - best action found using an AND-OR tree - board-evaluation * probability-of-board.
     *      - If the best option is to not to attack, make a movement and end the turn.
     */

    
    private static int threshold = 1;

    /**
     * The AI calcualtes a series of Reinforcements it wishes to do 
     * on the given Board for the given Player. 
     * @param board - the Board.
     * @param player - the Player the AI places reinforcements for.
     */
    public static Reinforcement[] reinforce(Board board, Player player){
        ArrayList<Reinforcement> reinfocements = new ArrayList<>(); // holds the desired distribution of reinforcements
        Board copy = board.copy();
        Land weakestLand;
        int numReinforcements = board.countReinforcements(player);

        // given the weakest Land one reinforcement. After each reinforcement copy is 
        // updated to prevent the same land from getting all reinfocements (unless it is always the weakest)
        while(numReinforcements > 0){   // repeat until all reinforcements are placed
            weakestLand = getWeakestLand(copy, player);
            reinfocements.add(new Reinforcement(weakestLand, 1));  // adds 1 reinforcement to the weakest Land
            weakestLand.changeTroopCount(1);    // updates the troop count in the weakest Land in copy
            numReinforcements--;
        }

        return (Reinforcement[]) reinfocements.toArray();
    }

    /**
     * The AI finds the best Move from the given Board and returns it.
     * The Move is found through the construction of an AND-OR tree with the given depth.
     * @param board - the Board.
     * @param player - the Player the AI plays for.
     * @return the Move the AI wishes to perform or NULL if the AI ends its turn. 
     */
    public static Move nextMove(Board board, Player player, int depth){
        Move[] actions;    // all attacks possible
        int[] bestAttack;   // the index of the best attack action and the value of it
        Move movement = null;  // the movement Move to end the turn with. Initialised to NULL, since that is a valid return value
        Land weakestLand;   // the weakest Land
        ArrayList<Land> neighbours; // the neighbours of the weakest Land
        Land strongestNeighbour;

        actions = actions(board, player);
        bestAttack = OR_Search(board, actions, player, depth); 
        if(bestAttack[1] >= threshold){  // if the value of best attack is larger than the threshold, commit to attack
            return actions[bestAttack[0]];
        }

        // the best attack found was not good enough, make the final movevement move and end the turn
        // we want to fortify the weakest Land by moving troops there from a neighbour
        weakestLand = getWeakestLand(board, player);
        neighbours = weakestLand.getNeighbours(player, true);   // gets all neighbours owned by this player
        
        if(neighbours.size() > 0){  // weakest country has neighbours
            strongestNeighbour = neighbours.get(0);
            for(Land neighbour : neighbours){   // go through all neighbours
                if(neighbour.getTroopCount() > strongestNeighbour.getTroopCount()){ // the current neighbour is stronger than the strongest
                    strongestNeighbour = neighbour;
                }
            }
            // moves half the difference between the Lands troops counts from strongest to weakest
            movement = new Move(player, strongestNeighbour, weakestLand, (strongestNeighbour.getTroopCount()-weakestLand.getTroopCount())/2);
        }
        

        return movement;
    }

    /**
     * OR-node in the AND-OR search tree.
     * Returns a touple of ints containing the index and the value of the best Move found.
     *  [index, value]
     * @param board - the current state
     * @param player - the Player
     * @param depth - the remaining levels we wish to make in the AND-OR tree
     * @return touple of ints containing the index and value of the best Move found
     */
    private static int[] OR_Search(Board board, Move[] actions, Player player, int depth){
        Board[] results;    // all possible results from performing this action
        int[] values = new int[actions.length];    // the values of each AND-node
        int index;  // index of the best action

        for(int i=0; i<actions.length; i++){ // simulate each action
            results = results(board, actions[i]);
            values[i] = AND_Search(results, player, depth-1); // the value of this action is the value of the worst case result of this action
        }

        index = maxIndex(values);
        return new int[] {index, values[index]};   // return the index of the action with the highest wost-case value - playing it safe (higher value means better Board)
    }

    /**
     * AND-node in the AND-OR search tree.
     * If the current depth is is the lowest we go, calculate the value of each state and return the smallest.
     * @param results - the list of Boards resulting from an action
     * @param player - the Player
     * @param depth - determines whether we calculate value now or make another level
     * @return the worst-case value of the action generating the given results
     */
    private static int AND_Search(Board[] results, Player player, int depth){
        Move[] actions; // actions that can be taken on the current Board
        int[] values = new int[results.length]; // the value of each OR-node
        
        if(depth > 0){  // we go down another level in the search tree
            for(int i=0; i<results.length; i++){
                actions = actions(results[i], player);
                values[i] = OR_Search(results[i], actions, player, depth)[1];
            }
        }
        else{   // we evaluate the the current results
            for(int i=0; i<results.length; i++){
                values[i] = evaluate(results[i], player);
            }
        }

        return min(values);
    }


    /**
     * Returns a list of all attack actions the AI can perform 
     * on the given Board for the given Player.
     * The given Board is not modified.
     * @param board - the Board
     * @param player - the Player the AI plays for.
     * @return an array of Moves the AI can perform on the given Board for the given Player
     */
    private static Move[] actions(Board board, Player player){
        ArrayList<Move> possibleActions = new ArrayList<>(); // all possible attack actions for the Player on the Board
        ArrayList<Land> borderingLands = board.getControlledBorderLands(player);    // all lands that are currently bordering a hostile territory
        ArrayList<Land> neighbours = new ArrayList<>(); // the neighbours of the current Land
        int troopCount; // the troop count on the current Land

        for(Land l : borderingLands){
            troopCount = l.getTroopCount();
            neighbours = l.getHostileNeighbours();
            for(Land neighbour : neighbours){
                if(troopCount == 2){ // l can attack with 1 troop
                    possibleActions.add(new Move(player, l, neighbour, 1));
                }
                if(troopCount == 3){ // l can attack with 2 troops
                    possibleActions.add(new Move(player, l, neighbour, 2));
                }
                if(troopCount >= 4){ // l can attack with 3 troops
                    possibleActions.add(new Move(player, l, neighbour, 3));
                }
            }
            
        }

        return (Move[]) possibleActions.toArray();
    }

    /**
     * Applies the given Move on the given Board and returns an array
     * with all the possible results from performing the Action of the Board.
     * Can only be used for attacks since movements do not cause any uncertainty.
     * The given Board will not be modified. 
     * @param board - the Board.
     * @param action - the action to perform on the Board.
     * @return an array of Boards representing all results from performing the given Action on the given Board.
     */
    private static Board[] results(Board board, Move action){
        ArrayList<Outcome> possibleOutcomes = ProbTable.getOutcomes(action.count, action.to.getTroopCount());   // all possible Outcomes from the given action
        ArrayList<Board> possibleBoards = new ArrayList<>();    // all states this action can lead to
        Board newBoard; // the new state after applying the current outcome
        Land cFrom; // the from Land in newBoard
        Land cTo;   // the to land in newBoard

        // goes through all possible outcomes and creates a new board where that outcome happend
        for(Outcome o : possibleOutcomes){
            newBoard = board.copy();
            cFrom = newBoard.getLandByName(action.from.getName());
            cTo = newBoard.getLandByName(action.to.getName());

            // carries out the attack
            cFrom.changeTroopCount(-o.attackersDying);
            cTo.changeTroopCount(-o.defendersDying);

            if(cTo.getTroopCount() == 0){  // attacker won and took the Land
                cTo.changeController(action.player);    // sets attacker as controller of the Land

                //moves one attacker over from the attacking Land
                cFrom.changeTroopCount(-1);
                cTo.changeTroopCount(1);
            }

            possibleBoards.add(newBoard);
        }

        return (Board[]) possibleBoards.toArray();
    }

    /**
     * Evaluates the given Board using some evaluation function.
     * The function determines how advantageous the Board currently 
     * is for the given Player.
     * @param board - the Board to evaluate
     * @return an evaluation of the gven Board from the given Players perspective
     */
    private static int evaluate(Board board, Player player){
        int value = 0;
        int troopCount = 0;
        
        value += board.getControlledLandsCount(player); // + the number of lands controlled
        value -= board.getControlledBorderLands(player).size(); // - the number of borders

        for(Land l : board.getControlledLands(player)){ // calculating total troops owned
            troopCount += l.getTroopCount();
        }
        
        value += troopCount * 0.25;    // + troop count times a factor

        return value;
    }

    /**
     * Finds the weakest border Land owned by the Player.
     * This is done by calculating the relative threat level each border Land is
     * under. This calculation is:
     *  neighbouring hostile troops / Land troop count
     * @param board - the Board
     * @param player - the Player 
     * @return the weakest Land owned by the Player
     */
    private static Land getWeakestLand(Board board, Player player){
        ArrayList<Land> borderLands = board.getControlledBorderLands(player); // a list of all border lands this player controls
        ArrayList<Land> neighbours = new ArrayList<>(); // the neighbours of the current land
        int[] borderingHostileTroopCount = new int[borderLands.size()];    // the number of hostile troops in bordering Lands. The indexes are one to one with borderLands
        int weakestLand;    // the index for the currently weakest Land
        int weakestLandRelativeThreat;  // the relative threat the currently weakest Land is under
        int currentLandRelativeThreat; // the relative theat the current Land is under

        // for each border land, calculate the number of hostile troops bordering it
        for(int i=0; i < borderLands.size(); i++){  // for each border land
            neighbours = borderLands.get(i).getHostileNeighbours();
            for(int j=0; j<neighbours.size(); j++){   // for each neighbour of this land
                borderingHostileTroopCount[i] += neighbours.get(j).getTroopCount();
            }
        }

        // find weakest land
        weakestLand = 0;
        for(int i=1; i<borderLands.size(); i++){    // goes through all border Lands but the first one
            
            // the relative threat is the sum of bordering hostiles divides by the troop count of the Land
            weakestLandRelativeThreat = borderingHostileTroopCount[weakestLand] / borderLands.get(weakestLand).getTroopCount();
            currentLandRelativeThreat = borderingHostileTroopCount[i] / borderLands.get(i).getTroopCount();
            if(currentLandRelativeThreat > weakestLandRelativeThreat){  // current Land is under a larger threat than weakest Land
                weakestLand = i;
            }
        }

        return borderLands.get(weakestLand);
    }

    /**
     * Returns the smallest element in the given array
     * @param arr - the array to find the smallest element in
     * @return the smallest element in arr
     */
    private static int min(int[] arr){
        int min = arr[0];
        for(int i=0; i<arr.length; i++)
            if(arr[i] < min)
                min = arr[i];
        return min;
    }

    /**
     * Returns the index of the largest element in the given array
     * @param arr - the array to find the index of the largest element in
     * @return the index of the largest element in arr
     */
    private static int maxIndex(int[] arr){
        int max = 0;
        for(int i=0; i<arr.length; i++)
            if(arr[i] > max)
                max = i;
        return max;
    }
}
