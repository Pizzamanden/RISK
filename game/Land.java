package game;
import java.util.*;

import containers.*;
import players.*;

public class Land {
    
    public int landID;  // id of the this Land
    private ArrayList<Land> borderingLand;  // Lands this land has borders to 
    private Player controller;  // the Player than controlles this Land
    private int troopCount; // the number of troops on this Land
    private String name;    // the nameo of this Land

    // Coordinates for drawing the board, and for deciding neighbours
    public Coordinate coords;


    /**
     * Constructor for a new Land.
     * All paramteres are explained above
     */
    public Land(String name, int landID, Player controller, Coordinate coords){
        this.borderingLand = new ArrayList<>();
        this.name = name;
        this.troopCount = 1;
        this.controller = controller;
        this.coords = coords;
        this.landID = landID;
    }

    /**
     * Determines whether this land has the specified land as a neighbour
     * @param neighbour - the Land we wish to check if it is a neighbour to this Land.
     * @return true if the given Land is a neighbour to this Land
     */
    public boolean hasNeighboringLand(Land neighbour){
        return borderingLand.contains(neighbour);
    }

    /**
     * With a list of lands as input, decide if a land by the given name exists in that list
     * @param lands - the list of Lands to check in
     * @param name - the anme of the Land to find
     * @return
     */
    public static Land getLandFromListByName(ArrayList<Land> lands, String name){
        Land land = null;
        int landIndex = 0;
        // Check the input up against all owned lands
        while(land == null && landIndex < lands.size()){
            if(lands.get(landIndex).getName().toLowerCase().equals(name)){
                land = lands.get(landIndex);
            }
            landIndex++;
        }
        return land;
    }


    /**
     * Returns a list of all neighbours for this Land. 
     * The list itself is not the same reference, but the neighbours are the references used.
     * Works exactly as a copy of the list of neighbours. The lands are still their same references, its just this list not being the same list as the one in the class
     * @return a list of neighbours for this Land
     */
    public ArrayList<Land> getNeighbours(){
        ArrayList<Land> list = new ArrayList<>();
        for (Land land : borderingLand) {
            list.add(land);
        }
        return list;
    }

    /**
     * Returns a list of either friendlt or hostile neighbours of this Land
     * The list itself is not the same reference, but the neighbours are the references used.
     * Works exactly as a copy of the list of neighbours. The lands are still their same references, its just this list not being the same list as the one in the class
     * @param player - the Player
     * @param ownedByThisPlayer - whether we want friendly or hostile neighbours
     * @return a list of either friendlt or hostile neighbours of this Land
     */
    public ArrayList<Land> getNeighbours(Player player, Boolean ownedByThisPlayer){
        ArrayList<Land> list = new ArrayList<>();
        for (Land land : borderingLand) {
            // This if-statement is strange. It has two conditions of true and two of false.
            // It is true if the player is indeed the same, and the boolean variable is true, OR if it is not owned by the same player, and the boolean is false.
            // If it is owned by the specified player, but the variable is false, the statement is false
            // So the statement compares the equivalence of the booleans of the players being the same and the parameter-boolean
            if((land.controller == player) == ownedByThisPlayer){
                list.add(land);
            }
        }
        return list;
    }

    /**
     * 
     * @return the Player in controll of this land
     */
    public Player getController(){
        return this.controller;
    }


    /**
     * checks if this Land has any hostile neighbour.
     * @return true if this Land has hostile neighbours, otherwise false. 
     */
    public boolean hasEnemyNeighbour(){
        boolean found = false;
        Iterator<Land> neighbourListIterable = borderingLand.iterator(); // Make iterator
        while(!found && neighbourListIterable.hasNext()){
            if (neighbourListIterable.next().controller != this.controller){
                found = true;
            }
        }
        return found;
    }


    /**
     * Changes the troop count of this land by the specified amount
     * Positive number increases the troop count, whilst negative will decrease it
     * @param change - the desired change to troop count
     * @precondition - changeTroopCount does not check if the given change is legal or not. 
     */
    public void changeTroopCount(int change){
        this.troopCount = this.troopCount + change;
    }

    /**
     * Sets the Player that controls this Land to the given Player 
     * @param newController - the Player that will not control this Land
     */
    public void changeController(Player newController){
        this.controller = newController;
    }

    /**
     * Adds a neighbour to this Land
     * @param neighbour - the neigubour to add
     */
    public void addNeighbour(Land neighbour){
        borderingLand.add(neighbour);
    }

    /**
     * @return the the troop count of this land
     */
    public int getTroopCount(){
        return this.troopCount;
    }

    /**
     * @return the name of this Land
     */
    public String getName(){
        return this.name;
    }


    /**
     *  Land equality consists of:
     *  - Controller
     *  - Troop count
     *  - Name
     *  - ID
     *  - Size of neighbour list
     *  Crucially, it does not consist of checking these neighbours. Only the Board containing the lands can know for sure
     */
    @Override
    public boolean equals(Object other){
        if(other == null)
			return false;
		if(other == this)
			return true;
		if(!(other instanceof Land))
			return false;
		Land otherLand = (Land) other;
        if(otherLand.controller != this.controller) // This is references matched, which is correct behavior for checking player equality
            return false;
        if(otherLand.troopCount != this.troopCount)
            return false;
        if(!otherLand.name.equals(this.name))
            return false;
        if(otherLand.landID != this.landID)
            return false;
        if(otherLand.borderingLand.size() != this.borderingLand.size())
            return false;

        return true;
    }
}
