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
        ArrayList<Reinforcement> reinfocements = new ArrayList<>(); // holds the desired distribution of reinforcements.
        ArrayList<Land> borderLands = board.getControlledBorderLands(player); // a list of all border lands this player controls.
        ArrayList<Land> neighbours = new ArrayList<>(); // the neighbours of the current land
        int[] borderLandsTroopCount = new int[borderLands.size()];   // holds the troop count for each bordering land. The indexes are one to one with borderLands
        int[] borderingHostileTroopCount = new int[borderLands.size()];    // the number of hostile troops in bordering Lands. The indexes are one to one with borderLands
        int hostileTroops;  // the number of hostile troops bordering a land
        int numReinforcements = board.countReinforcements(player);  // the reinforcements available for the Player
        int weakestLand;    // the index for the currently weakest Land
        int weakestLandRelativeThreat;  // the relative threat the currently weakest Land is under
        int relativeThreat; // the relative theat the current Land is under

        // sets borderLandsTroopCount
        for(int i=0; i < borderLands.size(); i++){
            borderLandsTroopCount[i] = borderLands.get(i).getTroopCount();
        }

        // for each border land, calculate the number of hostile troops bordering it
        for(int i=0; i < borderLands.size(); i++){  // for each border land
            neighbours = borderLands.get(i).getHostileNeighbours();
            hostileTroops = 0;
            for(Land neighbour : neighbours){   // for each neighbour of this land
                hostileTroops = hostileTroops + neighbour.getTroopCount();
            }
            borderingHostileTroopCount[i] = hostileTroops;
        }

        // the lands with the lowest deffensive strength will get 1 reinforcement
        weakestLand = 0;
        while(numReinforcements > 0){   // repeat until all reinforcements are placed

            // the relative threat is the sum of bordering hostiles divides by the troop count of the Land
            weakestLandRelativeThreat = borderingHostileTroopCount[weakestLand] / borderLandsTroopCount[weakestLand];

            // finds the weakest Land
            for(int i=1; i<borderLands.size(); i++){    // goes through all border Lands
                relativeThreat = borderingHostileTroopCount[i] / borderLandsTroopCount[i];
                if(relativeThreat > weakestLandRelativeThreat){ // the current Land is under more threat that the weakest Land
                    weakestLand = i;
                }
            }

            reinfocements.add(new Reinforcement(borderLands.get(weakestLand), 1));  // adds one reinforcement to the weakest Land
            borderLandsTroopCount[weakestLand]++;   // updates the troop count of the weakest Land
        }

        return (Reinforcement[]) reinfocements.toArray();
    }

    /**
     * The AI finds the best Moves from the given Board and returns it.
     * Only the last Move in the list can be an attack move, all other 
     * Moves are movement Moves. 
     * The movements Moves are calculated based on the state of the given Board.
     * The attack move is found through the construction of an AND-OR tree.
     * @param board - the Board.
     * @param player - the Player the AI plays for.
     * @return an array of Moves the AI wishes to perform. 
     */
    public static Move[] move(Board board, Player player){
        ArrayList<Move> moves = new ArrayList<>();
        Move[] actions;
        Board[] results;

        // figure how to do the movement parts. Might want getWeakestLand and getStrongestLand methods for it. 
        // implement AND-OR tree search for the best attack action

        return (Move[]) moves.toArray();
    }

    /**
     * Returns a list of all attack actions the AI can perform 
     * from the given Board for the given Player
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
        return 0;
    }
}
