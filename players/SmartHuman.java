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
     *  Implement the move() method required of a player
     */
    @Override
    public Move move(Board board){
        while(true){
            
            System.out.print("What would you like to do? ");
            String input = scanner.nextLine().toLowerCase();
            if(input.equals("help")){
                System.out.println("You can type your command as 2 names of lands followed by a number, with each seperated by spacebar, to issue a command.");
                System.out.println("The command is then interpreted as either a movement or an attack, depending on what it can be.");
                System.out.println("Example: \"Germany Poland 3\" (without the brackets) to move/attack from Germany to Poland with 3 troops.");
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
                Game.printGivenGame(board);
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
            // Successful parse, now check the legality of the input
            // Quickly check that the troop count is above 0
            if(troopCount < 1){
                System.out.println("You must use 1 or more troops for any action. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            Land fLand = board.getLandByName(splicedInput[0]);
            Land tLand = board.getLandByName(splicedInput[1]);
            // If the land cannot be found, getLandByName can return null, check for that first
            if(fLand == null){
                System.out.println("Could not find the land you are trying to make an action from. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            if(tLand == null){
                System.out.println("Could not find the land you are trying to make an action to. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            // The first number is the starting land
            // This player must always control the source of an action
            if(fLand.getController() != this){
                System.out.println("You do not control the land you are trying to make an action from. Try again");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            // Check that the source land has more than 1 troop
            if(fLand.getTroopCount() < 2){
                System.out.println("The specified land does not have enough troops for any order. Try specifying another source land.");
                System.out.println("To view all available commands, type \"help\" (without the brackets)");
                continue;
            }
            // The controller of the target determines the action
            if(tLand.getController() == this){
                // Movement order
                // The targeted land must be connected for this to be legal
                if(!fLand.isConnectedTo(tLand)){
                    System.out.println("The land specified as the target is not connected to this land. You cannot move troops between non-connected lands");
                    System.out.println("To view all available commands, type \"help\" (without the brackets)");
                    continue;
                }
                System.out.println("You are trying to move " + troopCount + " troops from " + fLand.getName() + " to " + tLand.getName());
                System.out.println("There is currently " + tLand.getTroopCount() + " troops in the targeted land.");
            } else { // The land exists, and we are not the controller.
                // Attack order
                // The targeted land must a neighbour of the source land for this to be legal
                if(!fLand.hasNeighboringLand(tLand)){
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