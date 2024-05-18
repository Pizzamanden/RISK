package game;
import java.util.*;

import containers.*;
import players.*;

public class Land {
    
    public int landID;
    private ArrayList<Land> borderingLand;
    private Player controller;
    private int troopCount;
    private String name;

    // Coordinates for drawing the board, and for deciding neighbours
    public Coordinate coords;


    public Land(String name, int landID, Player controller, Coordinate coords){
        this.borderingLand = new ArrayList<>();
        this.name = name;
        this.troopCount = 1;
        this.controller = controller;
        this.coords = coords;
        this.landID = landID;
    }


    /*
     *  Determines whether this land has the specified land as a neighbour
     *  Uses a simple loop with a short-circuit to check
     */
    public boolean hasNeighboringLand(Land neighbour){
        boolean found = false;
        Iterator<Land> neighbourListIterable = borderingLand.iterator(); // Make iterator
        while(!found && neighbourListIterable.hasNext()){
            if (neighbour == neighbourListIterable.next()){ // This comparison is warrented, as comparing references is the absolute way of knowing equality
                found = true;
            }
        }
        return found;
    }

    /*
     *  With a list of lands as input, decide if a land by the given name exists in that list
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


    /*
     *  Implements a breadth-first-search to return a hashset of all lands which are connected to this land
     *  Can terminate early on a successful find
     *  Does not return the set of connected land, use connectedLand().contains() if both check and set is needed
     *  If this land and the parameter does not share owner, this will always be false
     */
    public boolean isConnectedTo(Land dest){
        boolean isConnectedTo = false;
        ArrayList<Land> visited = new ArrayList<>();
        Queue<Land> q = new LinkedList<>();
        // Add the starting land to queue and visited set
        q.add(this);
        visited.add(this);
        // Go as long as the queue is not empty
        while(!q.isEmpty() && !isConnectedTo){
            // Take the head of the queue. This land is always already marked as visited
            Land currentLand = q.remove();
            // Get all neighbours for this land
            for (Land neighbour : currentLand.getNeighbours()) {
                // If the neighbour has already been visited, disregard it
                // If not, and it is owned by the same player as this land, add it to the queue and to the visited list
                if(!visited.contains(neighbour) && neighbour.getController()==this.getController()){
                    q.add(neighbour);
                    visited.add(neighbour);
                    if(neighbour == dest){
                        isConnectedTo = true;
                    }
                }
            }
        }
        return isConnectedTo;
    }

    /*
     *  Implements a breadth-first-search to return a hashset of all lands which are connected to this land
     *  Does not include this land in the list
     */
    public ArrayList<Land> getAllConnectedLand(){
        ArrayList<Land> visited = new ArrayList<>();
        Queue<Land> q = new LinkedList<>();
        // Add the starting land to queue and visited set
        q.add(this);
        visited.add(this);
        // Go as long as the queue is not empty
        while(!q.isEmpty()){
            // Take the head of the queue. This land is always already marked as visited
            Land currentLand = q.remove();
            // Get all neighbours for this land
            for (Land neighbour : currentLand.getNeighbours()) {
                // If the neighbour has already been visited, disregard it
                // If not, and it is owned by the same player as this land, add it to the queue and to the visited list
                if(!visited.contains(neighbour) && neighbour.getController()==this.getController()){
                    q.add(neighbour);
                    visited.add(neighbour);
                }
            }
        }
        // Now remove this land from the list of visited lands.
        // This is only because of how the game works, which spares a lot of headache if we do not have the option of making loops of actions.
        visited.remove(this);
        return visited; // Return all seen lands
    }

    /*
     *  Returns a list of neighbours
     *  The list itself is not the same reference, but the neighbours are the references used.
     */
    public ArrayList<Land> getNeighbours(){
        ArrayList<Land> list = new ArrayList<>();
        for (Land land : borderingLand) {
            list.add(land);
        }
        return list;
    }

    public boolean isLandAttackTarget(Land target, Player attacker){
        ArrayList<Land> neighbours = this.getNeighbours();
        return (neighbours.contains(target) && target.getController() != attacker);
    }

    /*
     *  Returns the controller of this land
     */
    public Player getController(){
        return this.controller;
    }

    /*
     * Changes the troop count of this land by the specified amount
     * Positive number increases the troop count, whilst negative will decrease it
     */
    public void changeTroopCount(int change){
        this.troopCount = this.troopCount + change;
    }

    public void changeController(Player newController){
        this.controller = newController;
    }

    public void addNeighbour(Land neighbour){
        borderingLand.add(neighbour);
    }


    /*
     * Returns the troop count of this land
     */
    public int getTroopCount(){
        return this.troopCount;
    }

    public String getName(){
        return this.name;
    }
}
