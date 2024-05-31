package game;
import java.util.*;

import containers.*;
import players.*;

public class Board {
    
    private ArrayList<Land> lands;  // all Lands on this board
    private int playerTurn = 0; // Tracks whos turn it currently is
    private int playerCount;    // the number of players in the game
    private ArrayList<Coordinate> centersInUse; // used to keep track of where Lands are placed
    private int landCountForReinforcement; // the number of Lands required to gain an additional reinforcement

    /*
     *  Constructor for a new, premade, board
     */
    public Board(ArrayList<Player> players, int landCountForReinforcement){
        this.lands = new ArrayList<>();
        this.playerCount = players.size();
        this.centersInUse = new ArrayList<>();
        this.landCountForReinforcement = landCountForReinforcement;
        
        // ID is written as number - 1, since this was translated from an image.
        generateLand(new Coordinate(2, 13), 1-1, players.get(0), "America");
        generateLand(new Coordinate(5, 13), 2-1, players.get(0), "Belgium");
        generateLand(new Coordinate(8, 15), 3-1, players.get(0), "Cambodia");
        generateLand(new Coordinate(11, 14), 4-1, players.get(0), "Denmark");
        generateLand(new Coordinate(8, 12), 5-1, players.get(0), "Estonia");
        generateLand(new Coordinate(0, 10), 6-1, players.get(0), "Finland");
        generateLand(new Coordinate(3, 10), 7-1, players.get(0), "Germany");
        generateLand(new Coordinate(0, 7), 8-1, players.get(0), "Haiti");

        generateLand(new Coordinate(3, 7), 9-1, players.get(1), "Ireland");
        generateLand(new Coordinate(3, 4), 10-1, players.get(1), "Japan");
        generateLand(new Coordinate(6, 9), 11-1, players.get(1), "Kenya");
        generateLand(new Coordinate(9, 9), 12-1, players.get(1), "Libya");
        generateLand(new Coordinate(12, 9), 13-1, players.get(1), "Malta");
        generateLand(new Coordinate(6, 6), 14-1, players.get(1), "Netherlands");
        generateLand(new Coordinate(10, 6), 15-1, players.get(1), "Oman");
        generateLand(new Coordinate(8, 3), 16-1, players.get(1), "Poland");
    }

    /*
     * Constructor for an empty board - used to copy the board
     */
    private Board(){
        lands = new ArrayList<>();
        centersInUse = new ArrayList<>();
    }


    private void generateLand(Coordinate coords, int id, Player startController, String name){
        //Add it to the list of coordinates we have already used
        centersInUse.add(coords);
        // Also generate the actual land, giving it these coordiantes
        Land newlyGenerated = new Land(name, id, startController, coords);
        // Generate its neighbours while we have it
        setNeighboursOfLand(newlyGenerated);
        // Add this one after generating its neighbours (important order, do not change, see method setNeighboursOfLand)
        lands.add(newlyGenerated);
    }

    /*
     *  Finds and sets all neighbours of the given land
     *  Important: Only use this method if the land has not yet been added to this.lands
     *  This is because this method does not check if the land being examined is the same as the parameter land
     */
    private void setNeighboursOfLand(Land land){
        for (Land possibleNeighbour : this.lands) { // For each existing coordinate.
            boolean bordering = false;
            // Just like when checking for collisions, we now check for the very specific collison of lying exactly in the ring 3 away from the center.
            // But only in one coordinate. In the other, it must be closer than 3. Otherwise, it is only touching corners or less.
            if((possibleNeighbour.coords.x == land.coords.x-3 || possibleNeighbour.coords.x == land.coords.x+3)){
                // Using the exact same technique as in isCoordinateAvailable(), we now want this specific collision
                // This checks that it does not lie further away than 2 in the y-coordinate in either positive or negative
                if(!(possibleNeighbour.coords.y < land.coords.y-2 || possibleNeighbour.coords.y > land.coords.y+2)){
                    bordering = true;
                }
            }
            if((possibleNeighbour.coords.y == land.coords.y-3 || possibleNeighbour.coords.y == land.coords.y+3)){
                // Using the exact same technique as in isCoordinateAvailable(), we now want this specific collision
                // This checks that it does not lie further away than 2 in the x-coordinate in either positive or negative
                if(!(possibleNeighbour.coords.x < land.coords.x-2 || possibleNeighbour.coords.x > land.coords.x+2)){
                    bordering = true;
                }
            }
            if(bordering){
                land.addNeighbour(possibleNeighbour);
                possibleNeighbour.addNeighbour(land);
            }
        }
    }


    /*
     *  Sets the turn to the next player, then returns that player number
     */
    public int nextPlayer(){
        playerTurn += 1;
        if(playerTurn > playerCount){
            playerTurn = 1;
        }
        return playerTurn;
    }


// - - - - - - - - - - - - - - - - - - Movements and attacks 


    /**
     * Checks if the given Move is a legal Move.
     * @param move - the move the Players wishes to make
     * @return true if the Move is legal, false otherwise.
     */
    public boolean isMoveLegal(Move move){
        // Does the specified move use a legal number of troops?
        if(move.count < 1){
            return false;
        }
        // Is there enough troops in the from-land to move any from it? 1 must remain on the land
        if(move.from.getTroopCount()-1 < move.count){
            return false;
        }
        // Does the current player control the source of the movement?
        if(move.from.getController() != move.player){
            return false;
        }
        // Target must be neighbour of source
        if(!move.from.hasNeighboringLand(move.to)){
            return false;
        }
        
        // Does the current player control the target?
        if(!(move.player == move.to.getController())){
            // This is an attack
            if(move.count > 3){
                return false;
            }
        } // At this point, all movements are legal

        return true;
    }



// - - - - - - - - - - - - - - - - - - Reinforcements 

    /*
     *  Method for checking legibility of a player placing reinforcements where they want to
     *  Returns false if the reinforcement is not legal
     */
    public boolean canReinforce(Player player, Reinforcement reinforcement, int remainingReinforcements){
        // Reinforcement amount must be larger than 0 and less than or equal to remaining reinforcements
        if(reinforcement.count <= 0 || reinforcement.count > remainingReinforcements){
            return false;
        }
        // The current player must control the land they attempt to reinforce
        return reinforcement.land.getController() == player;
    }

    /*
     *  Sums up the total amount of reinforcements a specified player would gain, 
     * if they were to gain refinforcements on this board. 
     */
    public int countReinforcements(Player player){
        int count = 3;
        return Math.max(count, getControlledLandsCount(player)/landCountForReinforcement);
    }

// - - - - - - - - - - - - - - - - - - Lists of the board, and other information 

    /**
     * Finds a land by name from the list of all lands
     * @param name - the name of the Land
     * @return the land object with the given name
     */
    public Land getLandByName(String name){
        Land foundLand = null;
        for (Land land : this.lands) {
            if(land.getName().toLowerCase().equals(name)){
                foundLand = land;
            }
        }
        return foundLand;
    }

    /**
     * A method which returns a list of all the lands the specified player controls
     *  Accepts a given player, which can be used to specify if its lands controlled by this player, or NOT by this player
     * @param player - the Player
     * @param ownedByThisPlayer - whether the Lands we are looking for are controlled by this player or another
     * @return a list of Lands that are either controlled by this player or not, depending on the boolean
     */
    public ArrayList<Land> getControlledLands(Player player, Boolean ownedByThisPlayer){
        ArrayList<Land> contLands = new ArrayList<>();
        for (Land land : this.lands) {
            if((land.getController() == player) == ownedByThisPlayer){ // See Land.getNeighbours for this logic
                contLands.add(land);
            }
        }
        return contLands;
    }

    /**
     * Returns the number of Lands controlled by the given Player
     * @param player - the Player
     * @return the number of Lands controlled by the given Player
     */
    public int getControlledLandsCount(Player player){
        return getControlledLands(player, true).size();
    }

    /**
     * Returns an ArrayList of all Lands the given
     * Player controls that border hostile Lands.
     * @param player - the Player
     * @return an ArrayList of Lands that border hostile lands
     */
    public ArrayList<Land> getControlledBorderLands(Player player){
        ArrayList<Land> borderLands = new ArrayList<>();   // all Lands that border a hostile Land

        for(Land maybeBorderLand : getControlledLands(player, true)){  // goes through all Lands the Player controls
            if(maybeBorderLand.hasEnemyNeighbour()){
                borderLands.add(maybeBorderLand);
            }
        }

        return borderLands;
    }


    /**
     * @return the size of the board
     */
    public int getBoardSize(){
        return lands.size();
    }

    /**
     * Rolls the specified number of dice by getting a random number from
     * 1 to 6 (included) the specified number of times, and then returns a list 
     * with the results in the order they are achieved. 
     * @param count - the number of dice to roll
     * @return a list of results from rolling the specified number of dice
     */
    public static ArrayList<Integer> rollDice(int count) {
        Random rand = new Random();
        ArrayList<Integer> diceRolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            diceRolls.add(rand.nextInt(6) + 1); // Rolling a six-sided die
        }
        return diceRolls;
    }

    /**
     * Carries out the movement given by reducing troops 
     * in one Land and increasing in another. 
     * @param move - the movement to carry out
     */
    public void carryOutMovement(Move move){
        move.from.changeTroopCount(-move.count);
        move.to.changeTroopCount(move.count);
    }



    @Override
    public String toString(){
        String output = "\n";
        // Step 1: Find all 4 extreme coordinate values.
        // The highest and lowest x-coordinates, and the highest and lowest y-coordinates
        // Start by setting the first land as the initial one
        Coordinate highX = centersInUse.get(0);
        Coordinate lowX = centersInUse.get(0);
        Coordinate highY = centersInUse.get(0);
        Coordinate lowY = centersInUse.get(0);

        for (Coordinate coord : centersInUse) {
            if(coord.x > highX.x){
                highX = coord;
            } else if(coord.x < lowX.x){
                lowX = coord;
            }
            if(coord.y > highY.y){
                highY = coord;
            } else if(coord.y < lowY.y){
                lowY = coord;
            }
        }
        int drawBoxWidth = (highX.x+1) - (lowX.x-1) + 1;
        int drawBoxHeight = (highY.y+1) - (lowY.y-1) + 1;
        int offsetX = 0-(lowX.x-1);
        int offsetY = 0-(lowY.y-1);
        char[][] box = new char[drawBoxHeight][drawBoxWidth];
        for (int y = 0; y < drawBoxHeight; y++) {
            for (int x = 0; x < drawBoxWidth; x++) {
                box[y][x] = ' ';
            }
        }
        
        // Now insert all lands
        for (Land land : lands) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    // For a y-coordinate, its distance to the lowY should be its distance to 0
                    // Which, translated to an easier approach, we can flip the number in the coordinates by just subtracting the offset coordinate from the size of the box
                    // If the actual coordinate is -3, with the offset of 4, that means the lowest number was -3.
                    // That gives a distance of 0. Since we are using the box, we need to subtract one, as we added 1 when we made it, to have room for the actual squares.
                    char toSet = (char) (land.landID+97);
                    box[drawBoxHeight-(land.coords.y+offsetY+i)-1][land.coords.x+offsetX+j] = toSet;
                } 
            }
        }
        // Now make the string:
        for (char[] cs : box) { // This iterates over the rows
            output = output + new String(cs) + "\n";
        }
        return output;
    }

    /**
     * Method for evaluating equality in boards
     *  The factors to consider are:
     *  - Is it the same player's turn?
     *  - Are the same lands owned by the same players?
     *  - Do these same lands have the same troop count?
     *  Warning: The time of calling this method can actually play a role. This does not track remaining reinforcements, which could be a problem
     */
    @Override
    public boolean equals(Object other){
        if(other == null)
			return false;
		if(other == this)
			return true;
		if(!(other instanceof Board))
			return false;
		Board otherBoard = (Board) other;
        if(otherBoard.lands.size() != this.lands.size())
            return false;
        if(otherBoard.playerTurn != this.playerTurn)
            return false;
        boolean landsEqual = true;
        int thisLandIndex = 0;
        while(landsEqual && thisLandIndex < this.lands.size()){ // While we still have lands to check, and we have not found a mismatch, keep going
            // The goal here is to check that each land has a mirror in the other board's list of lands
            Land cLand = lands.get(thisLandIndex);
            if(!otherBoard.lands.contains(cLand)){
                // Contains uses the equals function for lands.
                // If we enter here, the otherBoard did not contain this land.
                landsEqual = false;
            }
        }
        return landsEqual;
    }


    /**
     * Creates and returns a deep copy of this Board.
     * @return a deep copy of this Board
     */
    public Board copy(){
        Board copy = new Board();

        // Copies all lands
        for(Coordinate c : this.centersInUse){  // copies the centers in use
            copy.centersInUse.add(new Coordinate(c.x, c.y));
        }

        // Since we cannot just copy one land, making Land.copy is bad. Instead we copy the lands here
        for(Land l : this.lands){   // copies the lands
            Land landCopy = new Land(
                l.getName(),
                l.landID,
                l.getController(),  // shallow copy of the Player.
                new Coordinate(l.coords.x, l.coords.y)
            );
            landCopy.changeTroopCount(l.getTroopCount()-1);
            copy.lands.add(landCopy);
        }

        // Setup neighbours for all the newly copied lands
        for(Land l : this.lands){   // copies all neighbours of all lands
            Land lCopy = copy.getLandByName(l.getName());    // copy's version of Land l
            for(Land n : l.getNeighbours()){    // adds the neighbours
                Land nCopy = copy.getLandByName(n.getName());    // copy's version of Land n
                lCopy.addNeighbour(nCopy);  // adds copy's n as neighbour for copy's l
            }
        }

        // copies meta information
        copy.playerTurn = this.playerTurn;
        copy.playerCount = this.playerCount;
        copy.landCountForReinforcement = this.landCountForReinforcement;

        return copy;
    }

}
