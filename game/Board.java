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
        
        generateLand(new Coordinate(2, 13), 1-1, players.get(0), "a");
        generateLand(new Coordinate(5, 13), 2-1, players.get(0), "b");
        generateLand(new Coordinate(8, 15), 3-1, players.get(0), "c");
        generateLand(new Coordinate(11, 14), 4-1, players.get(0), "d");
        generateLand(new Coordinate(8, 12), 5-1, players.get(0), "e");
        generateLand(new Coordinate(0, 10), 6-1, players.get(0), "f");
        generateLand(new Coordinate(3, 10), 7-1, players.get(0), "g");
        generateLand(new Coordinate(0, 7), 8-1, players.get(0), "h");

        generateLand(new Coordinate(3, 7), 9-1, players.get(1), "i");
        generateLand(new Coordinate(3, 4), 10-1, players.get(1), "j");
        generateLand(new Coordinate(6, 9), 11-1, players.get(1), "k");
        generateLand(new Coordinate(9, 9), 12-1, players.get(1), "l");
        generateLand(new Coordinate(12, 9), 13-1, players.get(1), "m");
        generateLand(new Coordinate(6, 6), 14-1, players.get(1), "n");
        generateLand(new Coordinate(10, 6), 15-1, players.get(1), "o");
        generateLand(new Coordinate(8, 3), 16-1, players.get(1), "p");
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
     *  TODO decide if the logic for all reinforcement checking should occur here, or if error-messages should be tailored, and thus be in the game logic
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
     *  Sums up the total amount of reinforcements a specified player would gain, if they were to reinforce right now
     */
    public int countReinforcements(Player player){
        int count = 3;
        return Math.max(count, getControlledLandsCount(player)/landCountForReinforcement);
    }

// - - - - - - - - - - - - - - - - - - Actions that can be performed

    public ArrayList<Land> getListOfActionableLands(Player player){
        ArrayList<Land> candidateLands = new ArrayList<>();
        for (Land land : this.lands) {
            if(land.getController() == player && land.getTroopCount() > 1){
                candidateLands.add(land);
            }
        }
        return candidateLands;
    }
    
    /*
     *  With a move and a specific outcome, return a copy of the board where this has happened
     */
    public Board applyOutcomeOnBoard(Move move, Outcome outcome){
        Board newBoard = this.copy();
        Land newAttLand = newBoard.lands.get(newBoard.lands.indexOf(move.from));
        Land newDefLand = newBoard.lands.get(newBoard.lands.indexOf(move.to));
        // Attacking land has lost as many troops as attackers have died
        newAttLand.changeTroopCount(-outcome.attackersDying);
        // Defender land has lost as many troops as defenders have died
        newDefLand.changeTroopCount(-outcome.defendersDying);
        // Did the land change hands?
        // Get the land in the copy that matches the land in the move.to
        if(newDefLand.getTroopCount() == 0){
            newAttLand.changeTroopCount(-1); // Move the guy who captures
            newDefLand.changeTroopCount(1); // He moves here
            newDefLand.changeController(move.player); // Now owned by the attacker
        }
        // Return the copy with the change
        return newBoard;
    }


// - - - - - - - - - - - - - - - - - - Lists of the board, and other information 



    /*
     *  Find a land by name from the list of all lands
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

    /*
     *  A method which returns a list of all the lands the specified player controls
     */
    public ArrayList<Land> getControlledLands(Player player){
        ArrayList<Land> contLands = new ArrayList<>();
        for (Land land : this.lands) {
            if(land.getController() == player){
                contLands.add(land);
            }
        }
        return contLands;
    }

    /*
     *  A method which returns a list of all the lands the specified player controls
     *  Accepts a given player, which can be used to specify if its lands controlled by this player, or NOT by this player
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
     * Returns an ArrayList of all Lands the given
     * Player controls that border hostile Lands.
     * @param player - the Player
     * @return an ArrayList of Lands that border hostile lands
     */
    public ArrayList<Land> getControlledBorderLands(Player player){
        ArrayList<Land> borderLands = new ArrayList<>();   // all Lands that border a hostile Land

        for(Land maybeBorderLand : getControlledLands(player)){  // goes through all Lands the Player controls
            if(maybeBorderLand.hasEnemyNeighbour()){
                borderLands.add(maybeBorderLand);
            }
        }

        return borderLands;
    }

    /*
     *  
     */
    public ArrayList<ArrayList<Land>> getConnectedLandZones(Player player){
        ArrayList<Land> ownedLands = this.getControlledLands(player);
        ArrayList<ArrayList<Land>> connectedZones = new ArrayList<>(); // Make the list of zones. A zone is a list, so this is a list of lists
        for (Land land : ownedLands) {
            // For each owned land, check if it is part of a zone we know of, or if it is part of a new zone
            if(connectedZones.size() == 0){
                // If there are no found zones yet, make the first
                ArrayList<Land> newZone = land.getAllConnectedLand();
                newZone.add(land); // getAllConnectedLand removes the original caller, so add it again
                connectedZones.add(newZone);
            } else {
                for (ArrayList<Land> zone : connectedZones) { // Now check if this current land is part of any current zone
                    if(!zone.contains(land)){
                        // New zone found, add it
                        ArrayList<Land> newZone = land.getAllConnectedLand();
                        newZone.add(land); // getAllConnectedLand removes the original caller, so add it again
                        connectedZones.add(newZone);
                    }
                }
            }
        }
        return connectedZones;
    }

    /*
     *  Counts the amount of land a player controls
     */
    public int getControlledLandsCount(Player player){
        return getControlledLands(player).size();
    }


    /*
     *  Return the size of the list of land
     */
    public int getBoardSize(){
        return lands.size();
    }

    // STATIC METHOD FOR ROLLING A SET OF DICE
    // Method to roll a specified number of dice
    public static ArrayList<Integer> rollDice(int count) {
        Random rand = new Random();
        ArrayList<Integer> diceRolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            diceRolls.add(rand.nextInt(6) + 1); // Rolling a six-sided die
        }
        return diceRolls;
    }

    /*
     *  This call is only actually done in the game.
     *  But an AI would maybe like easy access to this when copying boards
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
                    char toSet = ' ';
                    toSet = (char) (land.landID+97);
                    // Logic below for setting each one of the 9 tiles a land owns
                    // if(i == 0 && j == 0){
                    //     // Middle
                    //     toSet = (char) (land.landID+97);
                    // } else if(i == 0){
                    //     // Right and left middle
                    //     toSet = '|';
                    // } else if(j == 0){
                    //     // Bottom and top middle
                    //     toSet = '-';
                    // } else if(i == -1 && j == -1){
                    //     // Top left
                    //     // toSet = '\\';
                    //     toSet = '+';
                    // } else if(i == 1 && j == -1){
                    //     // Bottom left
                    //     // toSet = '/';
                    //     toSet = '+';
                    // } else if(i == -1 && j == 1){
                    //     // Top right
                    //     // toSet = '/';
                    //     toSet = '+';
                    // } else if(i == 1 && j == 1){
                    //     // Bottom right
                    //     // toSet = '\\';
                    //     toSet = '+';
                    // }
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

    /*
     *  Method for evaluating equality in boards
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
