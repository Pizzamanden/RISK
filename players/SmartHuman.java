package players;
import java.util.*;

import containers.*;
import game.*;

public class SmartHuman extends Player{

    
    private Scanner scanner = new Scanner(System.in);
    
    /*
     *  Constructor demanded by the Player class
     */
    public SmartHuman(int assignedNumber) {
        super(assignedNumber);
    }

    /*
     *  Implement the attack() method required of a player
     */
    @Override
    public Move attack(Board board){
        while(true){
            
            System.out.print("\nWhat would you like to do? ");
            String input = scanner.nextLine().toLowerCase();
            if(input.equals("help")){
                System.out.println("You can type your command as 2 names of lands followed by a number, with each seperated by spacebar, to issue a command.");
                System.out.println("The command is then interpreted as either a movement or an attack, depending on what it can be.");
                System.out.println("Example: \"Germany Poland 3\" (without the brackets) to attack Poland from with 3 troops.");
                System.out.println("\nYou can also type \"done\" to end your turn, or \"map\" to view the map.");
                continue;
            }
            if(input.equals("done")){
                return null;
            }
            if(input.equals("map")){
                Game.printGivenGame(board);
                continue;
            }
            if(input.equals("status")){
                Game.printBoardStatus(board, this);
                continue;
            }
            // Split the input and check the validity of the result
            String[] splicedInput = input.split(" ");
            if(splicedInput.length != 3){
                System.out.println("Too many or too few arguments were used, or the singular command could not be understood. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            // Now parse the result to integers
            int troopCount = -1;
            try {
                troopCount = Integer.parseInt(splicedInput[2]);
            } catch (Exception e) {
                System.out.println("Error: Not all commands specified were numbers. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            
            // Check if the amount of troops specified is greater than 3. If it is, reduce it, but tell the player.
            if(troopCount > 3){
                System.out.println("You can attack with at most 3 troops at once. You can always make multiple attacks in the same turn.");
                System.out.println("The attack is set to be using 3 troops.");
                // We could just use Math.min(3, troopCount), but here we get to tell the player that their planned attack was edited, allowing them to reconsider.
                troopCount = 3;
            }
            
            Land fLand = board.getLandByName(splicedInput[0]);
            Land tLand = board.getLandByName(splicedInput[1]);
            
            // Successful parse, now check the legality of the input
            if(!checkValidity(fLand, tLand, troopCount)){
                // Just continue, since the method takes care of the printing
                continue;
            }

            // The controller of the target determines the action
            if(tLand.getController() == this){
                // This is a movement
                System.out.println("Only attacks are allowed in the attack phase. Try again.");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            } else { // The land exists, and we are not the controller.
                // Attack order
                // The targeted land must a neighbour of the source land for this to be legal
                if(!fLand.hasNeighboringLand(tLand)){ // We already checked that we do not control the target
                    System.out.println("The land specified as the target is not in range for this land to attack it. Consider moving troops to one of your bordering lands.");
                    System.out.println("To view all available commands, type \"help\" (without the brackets)");
                    continue;
                }
                System.out.println("You are trying to attack " + tLand.getName() + " from " + fLand.getName() + " with " + troopCount + " troops.");
                System.out.println("The enemy has " + tLand.getTroopCount() + " troops.");
            }
            System.out.println("Type \"Y\" to confirm this order. Type anything else to cancel it.");
            String confirmation = scanner.nextLine().toLowerCase();
            if(confirmation.equals("y") || confirmation.equals("yes")){
                return new Move(this, fLand, tLand, troopCount);
            }
        }
    }

    /*
     *  Implement the move() method required of a player
     */
    @Override
    public Move move(Board board){
        while(true){
            
            System.out.print("What would you like to do? ");
            String input = scanner.nextLine().toLowerCase();
            if(input.equals("help")){
                System.out.println("You can type your command as 2 names of lands followed by a number, with each seperated by spacebar, to issue a command.");
                System.out.println("The command has to be a legal movement of troops from one of your lands, to one of its neighbours.");
                System.out.println("Example: \"Germany Poland 3\" (without the brackets) to move 3 troops from Germany to Poland");
                System.out.println("\nYou can also type \"done\" to end your turn, or \"map\" to view the map, or \"status\" to view a list of the lands and their troop counts.");
                continue;
            }
            if(input.equals("done")){
                return null;
            }
            if(input.equals("map")){
                Game.printGivenGame(board);
                continue;
            }
            if(input.equals("status")){
                Game.printBoardStatus(board, this);
                continue;
            }
            // Split the input and check the validity of the result
            String[] splicedInput = input.split(" ");
            if(splicedInput.length != 3){
                System.out.println("Too many or too few arguments were used, or the singular command could not be understood. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            // Now parse the result to integers
            int troopCount = -1;
            try {
                troopCount = Integer.parseInt(splicedInput[2]);
            } catch (Exception e) {
                System.out.println("Error: Not all commands specified were numbers. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            Land fLand = board.getLandByName(splicedInput[0]);
            Land tLand = board.getLandByName(splicedInput[1]);

            // Successful parse, now check the legality of the input
            if(!checkValidity(fLand, tLand, troopCount)){
                continue;
            }

            // The controller of the target determines the action
            if(tLand.getController() == this){
                // Movement order
                // The targeted land must be connected for this to be legal
                if(!fLand.hasNeighboringLand(tLand)){
                    System.out.println("The land specified as the target is not a neighbour of this land. Try again.");
                    System.out.println("To view all available commands, type \"help\" (without the brackets)");
                    continue;
                }
                System.out.println("You are trying to move " + troopCount + " troops from " + fLand.getName() + " to " + tLand.getName());
                System.out.println("There is currently " + tLand.getTroopCount() + " troops in the targeted land.");
            } else { // The land exists, and we are not the controller.
                System.out.println("You are only allowed to move troops between friendly lands in this phase. Try again.");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
            }
            System.out.println("Type \"Y\" to confirm this order. Type anything else to cancel it.");
            String confirmation = scanner.nextLine().toLowerCase();
            if(confirmation.equals("y") || confirmation.equals("yes")){
                return new Move(this, fLand, tLand, troopCount);
            }
        }
    }

    /*
     *  Method for centralizing duplicated code.
     *  Handles all logic regarding 
     */
    private boolean checkValidity(Land from, Land to, int count){
        // Quickly check that the troop count is above 0
        if(count < 1){
            System.out.println("You must use 1 or more troops for any action. Try again");
            System.out.println("To view all available commands, type \"help\" (without the brackets)");
            return false;
        }
        // If the land cannot be found, getLandByName can return null, check for that first
        if(from == null){
            System.out.println("Could not find the land you are trying to make an action from. Try again");
            System.out.println("To view all available commands, type \"help\" (without the brackets)");
            return false;
        }
        if(to == null){
            System.out.println("Could not find the land you are trying to make an action to. Try again");
            System.out.println("To view all available commands, type \"help\" (without the brackets)");
            return false;
        }
        // The first number is the starting land
        // This player must always control the source of an action
        if(from.getController() != this){
            System.out.println("You do not control the land you are trying to make an action from. Try again");
            System.out.println("To view all available commands, type \"help\" (without the brackets)");
            return false;
        }
        // Check that the source land has enough troops for the order
        if(from.getTroopCount()-1 < count){
            System.out.println("The specified land does not have enough troops for the order. Try specifying another source land, or use less troops.");
            System.out.println("To view all available commands, type \"help\" (without the brackets)");
            return false;
        }
        return true;
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
            System.out.println("\nYou can also type \"map\" to view the map, or \"status\" to view a list of the lands and their troop counts.");

            System.out.print("\nType the name of a land you own: ");
            // Get an input from the player
            String input = scanner.nextLine().toLowerCase();
            
            if(input.equals("map")){
                Game.printGivenGame(board);
                continue;
            }
            if(input.equals("status")){
                Game.printBoardStatus(board, this);
                continue;
            }


            Land targetLand = Land.getLandFromListByName(ownedLands, input);
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