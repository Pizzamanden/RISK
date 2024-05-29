package players;
import java.util.*;

import containers.*;
import game.*;

public class Human extends Player{

    
    private Scanner scanner = new Scanner(System.in);
    
    /*
     *  Constructor demanded by the Player class
     */
    public Human(int assignedNumber) {
        super(assignedNumber);
    }

    /*
     *  Implement the move() method required of a player
     *  This method is optimized to give feedback to any human player on what they can do, and why what they are trying to do is not allowed
     *  Making this method print-heavy will spare the game itself
     */
    @Override
    public Move attack(Board board){
        while(true){
            System.out.println("\n");
            Game.printGivenGame(board);
            // Start by displaying a list of lands the player can interact with
            System.out.println("\nYou can type the name of one of these friendly lands to be the source of your attack");
            ArrayList<Land> borderLands = board.getControlledBorderLands(this);
            ArrayList<Land> attackEnabledLands = new ArrayList<>();
            for (Land land : borderLands) { // Go over all these, and display them with some info
                if(land.getTroopCount() > 1){ 
                    attackEnabledLands.add(land);
                    System.out.println(land.getName() + ": Current troop count: " + land.getTroopCount());
                }
            }
            System.out.println("Or type \"done\" (without the quotations) to end the attack phase.");
            System.out.println("Or type \"map\" (without the quotations) to view the current board.");
            System.out.print("\nWhat would you like to do? ");

            // The player input a desired from-land. Parse the input
            String srcLandName = scanner.nextLine().toLowerCase();
            if(srcLandName.equals("done")){
                return null;
            }
            if(srcLandName.equals("map")){
                Game.printGivenGame(board);
                continue;
            }
            Land fromLand = Land.getLandFromListByName(attackEnabledLands, srcLandName);
            if(fromLand == null){
                System.out.println("Sorry, but the provided name does not match any owned land, try again.");
                continue;
            }
            // They gave a land from the list.
            ArrayList<Land> fromLandEnemyNeighbours = fromLand.getNeighbours(this,false);
            System.out.println("You can now either move troops to a connected land you own, or attack an enemy adjacent land.");
            // Display a list of targets this land can attack
            System.out.println("\nThe list of targets for an attack are:");
            for (Land neighbour : fromLandEnemyNeighbours) {
                if(neighbour.getController() != this){
                    System.out.println(neighbour.getName() + ": Enemy troop count: " + neighbour.getTroopCount());
                }
            }

            // The player input a desired to-land. Parse the input
            System.out.print("\nWhat would you like to do? ");
            String destLandName = scanner.nextLine().toLowerCase();
            if(destLandName.equals("back")){
                continue;
            }
            // Now we analyse what if their target is legal
            Land targetLand = Land.getLandFromListByName(fromLandEnemyNeighbours, destLandName);
            if(targetLand == null){
                // The target is not in the list of neighbours
                System.out.println("This target cannot be attacked from here. Try again.");
                continue;
            }

            System.out.println("\nYou currently have " + fromLand.getTroopCount() + " troops at the source.");
            System.out.println("The enemy have " + targetLand.getTroopCount() + " troops.");
            System.out.println("You can attack with at most " + Math.min(fromLand.getTroopCount()-1, 3) + " troops.");
            
            for (int i = 0; i < 3; i++) {
                ArrayList<Outcome> outcomes = ProbTable.getOutcomes(i+1, Math.min(targetLand.getTroopCount(), 2));
                for (Outcome outcome : outcomes) {
                    System.out.print("Attacking with " + (i+1) + " troops has a ");
                    outcome.printProbAsPercentage();
                    System.out.print(" chance of losing " + outcome.attackersDying + " of your troops, and killing " + outcome.defendersDying + " of theirs.\n");
                }
                System.out.println("");
            }
            
            System.out.print("\nHow many troop do you want to send? ");

            // Player inputs a number of troops
            String troopCountString = scanner.nextLine();
            int parsedTroopCount = 0;
            try {
                parsedTroopCount = Integer.parseInt(troopCountString);
            } catch (Exception e) {
                System.out.println("Error: Please input a whole number, using digits only.");
                continue;
            }
            if(parsedTroopCount >= fromLand.getTroopCount() || parsedTroopCount < 1){
                System.out.println("Error: Please input a number that is at most one less that what you have, and is above 0.");
                continue;
            }
            return new Move(this, fromLand, targetLand, parsedTroopCount);
        }
    }

    /*
     *  Implement the move() method required of a player
     *  This method is optimized to give feedback to any human player on what they can do, and why what they are trying to do is not allowed
     *  Making this method print-heavy will spare the game itself
     */
    @Override 
    public Move move(Board board){
        while(true){
            System.out.println("\n");
            Game.printGivenGame(board);
            // Start by displaying a list of lands the player can interact with
            System.out.println("\nYou can type the name of one of these friendly lands to manage the soldiers there:");
            ArrayList<Land> actionableLands = board.getListOfActionableLands(this);
            for (Land land : actionableLands) { // Go over all these, and display them with some info
                System.out.println(land.getName() + ": Current troop count: " + land.getTroopCount());
            }
            System.out.println("Or type \"done\" (without the quotations) to end your turn.");
            System.out.println("Or type \"map\" (without the quotations) to view the current board.");
            System.out.print("\nWhat would you like to do? ");
            // Take and parse their order
            String srcLandName = scanner.nextLine().toLowerCase();
            if(srcLandName.equals("done")){
                return null;
            }
            if(srcLandName.equals("map")){
                Game.printGivenGame(board);
                continue;
            }
            Land fromLand = Land.getLandFromListByName(actionableLands, srcLandName);
            if(fromLand == null){
                System.out.println("Sorry, but the provided name does not match any owned land, try again.");
                continue;
            }
            // They gave a land from the list.
            ArrayList<Land> fromLandFriendlyNeighbours = fromLand.getNeighbours(this, true);

            // Display a list of lands this list can move troops to
            System.out.println("\nThe list of places to move troops to are:");
            for (Land connected : fromLandFriendlyNeighbours) {
                System.out.println(connected.getName() + ": Your troop count: " + connected.getTroopCount());
            }

            // Wait for their response
            System.out.print("\nWhat would you like to do? ");
            String destLandName = scanner.nextLine().toLowerCase();
            if(destLandName.equals("back")){
                continue;
            }

            // Now we analyse what if their target is legal
            Land targetLand = Land.getLandFromListByName(fromLandFriendlyNeighbours, destLandName);
            if(targetLand == null){
                // The target is not in the list of owned and connected land
                System.out.println("This target cannot be moved to from here. Try again.");
                continue;
            }

            System.out.println("\nYou currently have " + fromLand.getTroopCount() + " troops at the source.");
            
            System.out.println("You have " + targetLand.getTroopCount() + " troops at the destination.");
            System.out.println("You can move at most " + (fromLand.getTroopCount()-1) + " troops.");
            
            System.out.print("\nHow many troop do you want to move? ");

            String troopCountString = scanner.nextLine();
            int parsedTroopCount = 0;
            try {
                parsedTroopCount = Integer.parseInt(troopCountString);
            } catch (Exception e) {
                System.out.println("Error: Please input a whole number, using digits only.");
                continue;
            }
            if(parsedTroopCount >= fromLand.getTroopCount() || parsedTroopCount < 1){
                System.out.println("Error: Please input a number that is at most one less that what you have, and is above 0.");
                continue;
            }
            return new Move(this, fromLand, targetLand, parsedTroopCount);
        }
    }

    /*
     *  Implementing reinforcements mechanic for human players
     */
    @Override
    public Reinforcement reinforce(Board board, int reinforceRemaining) {
        ArrayList<Land> ownedLands = board.getControlledLands(this);
        while(true){
            for (int i = 0; i < ownedLands.size(); i++) {
                System.out.println(ownedLands.get(i).getName() + ": Current count: " + ownedLands.get(i).getTroopCount());
            }
            System.out.println("You have: " + reinforceRemaining + " reinforcements remaining. Where do you want to place them?");
            System.out.print("\nType the name of a land you own: ");
            // Get an input from the player
            String landName = scanner.nextLine().toLowerCase();
            Land targetLand = Land.getLandFromListByName(ownedLands, landName);
            if(targetLand == null){
                System.out.println("Sorry, but the provided name does not match any owned land, try again.");
                continue;
            }
            // This land is owned by this player
            System.out.println("\nHow many of the " + reinforceRemaining + " reinforcements should be placed here?");
            String chosenReinforcementCount = scanner.nextLine();
            if(chosenReinforcementCount.equals("Back")){ // The user can type "Back" if they regret choosing this land
                continue;
            }
            int parsedCount = -1;
            try {
                parsedCount = Integer.parseInt(chosenReinforcementCount);
            } catch (Exception e) {
                System.out.println("Error: The typed answer was not a number. Try again.");
                continue;
            }
            if(parsedCount > 0 && parsedCount <= reinforceRemaining){
                // The player gave a correct version of a reinforcement request. Return it and se if the game accepts it.
                return new Reinforcement(targetLand, parsedCount);
            } else {
                System.out.println("The number provided was not between 1 and the remaining amount. Try again.");

            }
        }
    }

    
}