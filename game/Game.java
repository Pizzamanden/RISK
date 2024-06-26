package game;
import java.util.*;

import containers.*;
import players.*;


public class Game {

    private Scanner scanner = new Scanner(System.in);
    private ArrayList<Player> players;

    // By using the one random number, using it again should make the same board. The dice rolls do not use the seed
    // For any combination of seed, playerCount and board size, a specific board should be generated
    private Board board;
    
    /*
     *  Constructor for the class
     *  Does nothing but call newGame with the same parameters
     */
    public Game(){
        this.players = new ArrayList<>();
        ProbTable.getOutcomes(1, 1);
        newGame();
        // ArrayList<ArrayList<Integer>> table = ProbTable.makeDiceSet(3);
        // for (ArrayList<Integer> rollSet : table) {
        //     for (int roll : rollSet) {
        //         System.out.print(roll + ", ");
        //     }
        //     System.out.print("\n");
        // }
    }

    /*
     *  Creates a new game on this object
     *  This is done by setting the variables again, whilst also generating a new board
     *  When this function is done, a call to startGame will then begin the game
     */
    public final void newGame(){
        int playerCount = 2;
        int currentPlayer = 0;
        // For all players, decide if they should be human or some AI
        while(currentPlayer < playerCount){
            System.out.println("Should player " + (currentPlayer+1) + " be a human? Y/N");
            String setPlayer = scanner.nextLine().toLowerCase();
            if(setPlayer.equals("y")){ // This player should be human
                this.players.add(new Human(currentPlayer+1));
                currentPlayer++;
            } else if(setPlayer.equals("n")) { // This player should be AI
                // Maybe here do more logic to choose AI type?
                this.players.add(new CompAggressive(currentPlayer+1, 8));
                currentPlayer++;
            } else {    
                // Input not allowed, loop again
                System.out.println("Sorry, that answer could not be understood. Try again");
            }
        }
        // Make the board now
        this.board = new Board(players, 2);
    }

    /*
     *  Start this game, and play until completion
     */
    public void startGame(){
        // Do initial phase

        // Now start the game
        while(!isGameOver()){
            // Whos turn is it?
            int playerNumber = board.nextPlayer();
            Player player = players.get(playerNumber-1); // Minus 1 because we save the number, not the index
            System.out.println("\nThe turn of player " + playerNumber + " begins.");

            // Print the board
            System.out.println("The board looks like the following:");
            printThisGame();

            // Calculate reinforcements
            int reinforcementsRemaining = board.countReinforcements(player);
            
            // Reinforce time!
            System.out.println("\nReinforcement phase begins.");
            while(reinforcementsRemaining > 0){ // While the player has reinforcements remaining, trap them here
                Reinforcement reinforcement = player.reinforce(board, reinforcementsRemaining); // The player tries to do a reinforcement
                // Use the method for checking if this reinforcement is legal
                if(board.canReinforce(player, reinforcement, reinforcementsRemaining)){
                    // Success!
                    reinforcementsRemaining = reinforcementsRemaining - reinforcement.count;
                    reinforcement.land.changeTroopCount(reinforcement.count);
                    System.out.println("Placed " + reinforcement.count + " troops in " + reinforcement.land.getName());
                } else {
                    System.out.println("The specified reinforcement could not be carried out.");
                    System.out.println("Please check that the specifiec troop amount is between 1 and the remaining amount, and that the targeted land is controlled by you.");
                }
            } // The logic inside this loop should make the loop terminate when the player has used all their reinforcements
            // Make the player give a move
            // If the move is NULL, it signifies an end to their turn.
            System.out.println("\nAction phase begins.");
            Move attemptMove = player.move(board);
            while(attemptMove != null){ // Null signifies that the turn is done.
                // Check that the move attempted is legal
                if(board.isMoveLegal(attemptMove)){
                    // Now identify the type of movement
                    if(attemptMove.to.getController() == attemptMove.player){
                        // This is a move order
                        carryOutMovement(attemptMove);
                        System.out.println("Your movement from " + attemptMove.from.getName() + " and to " + attemptMove.to.getName() + " with " + attemptMove.count + " troops was a success!");
                    } else {
                        // This is an attack order
                        carryOutAttack(attemptMove);
                        System.out.println("Your attack from " + attemptMove.from.getName() + " against " + attemptMove.to.getName() + " with " + attemptMove.count + " troops has concluded.");
                    }
                } else {
                    // Move was not a legal movement or an attack, but neither was it null
                    System.out.println("The specified move could not be executed. Try again.");
                }
                System.out.println("What would you like to do now?");
                attemptMove = player.move(board);
            }
            System.out.println("The turn of player " + playerNumber + " is over.");
            // Their turn is now over, after having reinforced, and having had the option to do as many moves as they want
        }
    }

    /*
     *  Carry out a movement.
     *  The board could be the one doing this, but having prints in any such case could force and AI to make prints when simulating.
     */
    public void carryOutMovement(Move move){
        move.from.changeTroopCount(-move.count);
        move.to.changeTroopCount(move.count);
    }
    
    /*
     *  Carry out a movement.
     *  The board could be the one doing this, but having prints in any such case could force and AI to make prints when simulating.
     */
    public void carryOutAttack(Move attack){
        // Roll their dice
        ArrayList<Integer> attackerRolls = Board.rollDice(attack.count);
        ArrayList<Integer> defenderRolls = Board.rollDice(Math.min(2,attack.to.getTroopCount()));
        // Print out the result of the attackers rolls
        System.out.print("The attacker rolled [");
        for (int i = 0; i < attackerRolls.size(); i++) {
            System.out.print(attackerRolls.get(i));
            if(i < attackerRolls.size()-1){
                System.out.print(", ");
            }
        }
        // Print out the result of the defenders rolls
        System.out.print("].\n");
        System.out.print("The defender rolled [");
        for (int i = 0; i < defenderRolls.size(); i++) {
            System.out.print(defenderRolls.get(i));
            if(i < defenderRolls.size()-1){
                System.out.print(", ");
            }
        }
        System.out.print("].\n");
        // Now compare their rolls
        int fights = defenderRolls.size();
        int attacksWon = 0;
        for (int i = 0; i < fights; i++) {
            int highestAttacker = Collections.max(attackerRolls);
            int highestAttackerIndex = attackerRolls.indexOf(highestAttacker);
            int highestDefender = Collections.max(defenderRolls);
            int highestDefenderIndex = defenderRolls.indexOf(highestDefender);
            System.out.println("\nComparing attacker roll of " + highestAttacker + " to highest defender roll of " + highestDefender);
            if(highestAttacker > highestDefender){
                System.out.println("The attacker won!");
                attacksWon++;
            } else {
                System.out.println("The defender won!");
            }
            attackerRolls.remove(highestAttackerIndex);
            defenderRolls.remove(highestDefenderIndex);
        }
        // Make the canges to the board
        attack.from.changeTroopCount(-(fights-attacksWon));
        attack.to.changeTroopCount(-attacksWon);
        // Display the result
        System.out.println("\nThe attacker lost " + (fights-attacksWon) + " troops and the defender lost " + attacksWon + " troops.");
        if(attack.to.getTroopCount() == 0){
            System.out.println("\nThe attacker has taken the land of " + attack.to.getName() + "!");
            attack.from.changeTroopCount(-1);
            attack.to.changeTroopCount(1);
            attack.to.changeController(attack.player);
        }
    }

    /*
     *  Directly prints out this game on call
     */
    public void printThisGame(){
        System.out.println(this.board.toString());
    }

    /*
     *  Print out the given board on call
     */
    public static void printGivenGame(Board board){
        System.out.println(board.toString());
    }

    /*
     *  Defines and checks whether this game is still going or not
     */
    private boolean isGameOver(){
        return (board.getControlledLandsCount(players.get(0)) == 0 || board.getControlledLandsCount(players.get(1)) == 0);
    }
}
