package players;
import game.*;
import containers.*;
import java.util.ArrayList;

/**
 * An attempt to make an AI :)
 */
public class MrAI extends AI{
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

    
    private int maxDepth;   // the deapth the AND-OR search goes to

    /**
     * Constructor for AI
     * @param depth - the max depth the AI goes to in the AND-OR tree search
     */
    public MrAI(int assignedNumber, int depth){
        super(assignedNumber);
        this.maxDepth = depth;
    }

    /**
     * The AI finds and gives the weakest Land one reinforcement
     * on the given Board for the given Player. 
     * @param board - the Board.
     * @param player - the Player the AI places reinforcements for.
     */
    public Reinforcement reinforce(Board board, int reinforcementsRemaining){
        return new Reinforcement(getWeakestLand(board, this), 1);
    }

    /**
     * AI finds the best attack action for the given Board.
     * This is done using an AND-OR tree search with a predetermined depth.
     * @param board - the Board
     * @return the best move for the given Board
     */
    public Move attack(Board board){
        ArrayList<Move> actions;    // all attacks possible
        int[] bestAttack;   // the index of the best attack action and the value of it

        actions = actions(board, this);
        if(actions.size() > 0){ // there are actions to do
            bestAttack = OR_Search(board, actions, maxDepth); 
            if(threshold(bestAttack[1], board)){  // if the value of best attack is worth it, commit to it
                return actions.get(bestAttack[0]);
            }
        }
        
        return null;    // the best attack is not good enough, or there are no actions, return null
    }

    /**
     * Finds the Move the AI wishes to perform from the given Board.
     * The AI will attempt to fortify its weakest Land, by pulling troops
     * from the weakest Lands strongest Neighbour.
     * @param board - the Board
     * @return the Move the AI wishes to make, or NULL if no Move is desired
     */ 
    public Move move(Board board){
        Move movement = null;  // the movement Move to end the turn with. Initialised to NULL, since no Move means we return NULL
        Land weakestLand;   // the weakest Land
        Land strongestNeighbour;    // the strongest Land connected to the weakest Land

        // we want to fortify the weakest Land by moving troops there from a neighbour
        weakestLand = getWeakestLand(board, this);
        strongestNeighbour = getStrongestNeighbour(board, this, weakestLand);
        
        if(weakestLand.equals(strongestNeighbour)){  // weakest Land has no connected Lands
            return null;    // we cannot fortify the weakest Land
        }

        if(strongestNeighbour.getTroopCount() > 1){ // strongest connected Land can move troops
            // moves 1 troop to the weakest Land from its strongest connected Land
            movement = new Move(this, strongestNeighbour, weakestLand, 1);
        }
        
        return movement;
    }

    /**
     * Evaluates the given Board using some evaluation function.
     * The function determines how advantageous the Board currently 
     * is for the given Player.
     * @param board - the Board to evaluate
     * @return an evaluation of the gven Board from the given Players perspective
     */
    public int evaluateBoard(Board board){
        int value = 0;
        int defendedBorderLands = 0;

        for(Land l : board.getControlledBorderLands(this)){
            if(1 < l.getTroopCount() && l.getTroopCount() < 4){
                defendedBorderLands++;
            }
        }
        
        value += board.getControlledLandsCount(this) * 2.5; // + the number of lands controlled
        value -= board.getControlledBorderLands(this).size() * 1.5; // - the number of borders
        value += defendedBorderLands * 2;   // + for each border than has more than 1 troop but less than 4 troops
        value += board.countReinforcements(this) * 2;   // + 2 for each reinfocement the AI can get from this board

        return value;
    }

    /**
     * Calculates the threshold for which an attack has to 
     * better than for the AI to commit to it.
     * Looks at the current Board and estimates if it is worth it to 
     * do the best attack found. 
     * @param value - the value of the best Move
     * @param board - the Board as it currently is
     * @return 
     */
    private boolean threshold(int value, Board board){
        // if the best value is larger than the value of the Board as it currently is, slight penalty to the current Board ot encourage aggresion
        return value >= (evaluateBoard(board) * 0.75);    
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
    private int[] OR_Search(Board board, ArrayList<Move> actions, int depth){
        ArrayList<Board> results;    // all possible results from performing this action
        int[] values = new int[actions.size()];    // the values of each AND-node
        int index;  // index of the best action

        for(int i=0; i<actions.size(); i++){ // simulate each action
            results = results(board, this, actions.get(i));
            values[i] = AND_Search(results, depth-1); // the value of this action is the value of the worst case result of this action
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
    private int AND_Search(ArrayList<Board> results, int depth){
        ArrayList<Move> actions; // actions that can be taken on the current Board
        int[] values = new int[results.size()]; // the value of each OR-node
        
        if(depth > 0){  // we go down another level in the search tree
            for(int i=0; i<results.size(); i++){
                actions = actions(results.get(i), this);

                if(actions.size() > 0){    // actions can be taken from this Board 
                    values[i] = OR_Search(results.get(i), actions, depth)[1];
                }
                else{   // no actions can be taken on this Board, we evaluate the Board
                    values[i] = evaluateBoard(results.get(i)); 
                }
            }
        }
        else{   // we evaluate the the current results
            for(int i=0; i<results.size(); i++){
                values[i] = evaluateBoard(results.get(i));
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
    private static ArrayList<Move> actions(Board board, Player player){
        ArrayList<Move> possibleActions = new ArrayList<>(); // all possible attack actions for the Player on the Board
        ArrayList<Land> borderingLands = board.getControlledBorderLands(player);    // all lands that are currently bordering a hostile territory
        ArrayList<Land> neighbours = new ArrayList<>(); // the hostile neighbours of the current Land
        int troopCount; // the troop count on the current Land

        for(Land l : borderingLands){
            troopCount = l.getTroopCount();
            neighbours = l.getNeighbours(player, false);
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

        return possibleActions;
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
    private static ArrayList<Board> results(Board board, Player player, Move action){
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

        return possibleBoards;
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
        double weakestLandRelativeThreat;  // the relative threat the currently weakest Land is under
        double currentLandRelativeThreat; // the relative theat the current Land is under

        if(borderLands.size() == 0){    // there are no border Lands. This player likely won the game
            return board.getControlledLands(player).get(0); // returns any Land, doesn't matter, the turn just has to finish
        }

        // for each border land, calculate the number of hostile troops bordering it
        for(int i=0; i < borderLands.size(); i++){  // for each border land
            neighbours = borderLands.get(i).getNeighbours(player, false);
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
     * Finds the strongest Land, owned by the Player, connected to the given Land .
     * This is done by calculating the relative threat level each border Land is
     * under. This calculation is:
     *  neighbouring hostile troops / Land troop count
     * @param board - the Board
     * @param player - the Player 
     * @return the strongest Land connected to the given Land
     */
    private static Land getStrongestNeighbour(Board board, Player player, Land land){
        ArrayList<Land> neighboursOfLand = land.getNeighbours(player, true); // a list of all border lands this player controls
        ArrayList<Land> neighbours = new ArrayList<>(); // the neighbours of the current land
        int[] borderingHostileTroopCount = new int[neighboursOfLand.size()];    // the number of hostile troops in bordering Lands. The indexes are one to one with connectedLands
        int strongestNeighbour;    // the index for the currently strongest Land
        double strongestNeighbourRelativeThreat;  // the relative threat the currently strongest Land is under
        double currentLandRelativeThreat; // the relative theat the current Land is under

        if(neighboursOfLand.size() == 0){    // there are no border Lands. This player likely won the game
            return land; // if there are no neighbours, land itself is the strongest neighbour
        }

        // for each friendly neighbour land, calculate the number of hostile troops bordering it
        for(int i=0; i < neighboursOfLand.size(); i++){  // for each friendly neighbour
            neighbours = neighboursOfLand.get(i).getNeighbours(player, false);
            for(int j=0; j<neighbours.size(); j++){   // for each neighbour of this land
                borderingHostileTroopCount[i] += neighbours.get(j).getTroopCount();
            }
        }

        // find strongest friendly neighbour
        strongestNeighbour = 0;
        for(int i=1; i<neighboursOfLand.size(); i++){    // goes through all friendly neighbours but the first one
            
            // the relative threat is the sum of bordering hostiles divides by the troop count of the neighbour
            strongestNeighbourRelativeThreat = borderingHostileTroopCount[strongestNeighbour] / neighboursOfLand.get(strongestNeighbour).getTroopCount();
            currentLandRelativeThreat = borderingHostileTroopCount[i] / neighboursOfLand.get(i).getTroopCount();
            if(currentLandRelativeThreat < strongestNeighbourRelativeThreat){  // current neighbour is under less threat than strongest neighbour
                strongestNeighbour = i;
            }
        }

        return neighboursOfLand.get(strongestNeighbour);
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
