package game;
import java.util.*;

import containers.*;
import players.*;

public class Board {
    
    private ArrayList<Land> lands;
    private int playerTurn = 0; // The only call needed is, at the start of a turn to get the next player, and save that value, using the method for it
    private int playerCount;
    private ArrayList<Coordinate> centersInUse;

    /*
     *  Constructor for a new board
     */
    public Board(ArrayList<Player> players){
        this.lands = new ArrayList<>();
        this.playerCount = players.size();
        this.centersInUse = new ArrayList<>();
        int landsToGenerate = 14;

        // First land is always a manual-added (0,0)
        Coordinate centerCoordinate = new Coordinate(0,0);
        lands.add(new Land("0", 14-landsToGenerate, (landsToGenerate-7 > 0 ? players.get(0) : players.get(1)), centerCoordinate));
        centersInUse.add(centerCoordinate);
        landsToGenerate--;

        
        Random rand = new Random();
        // Now the adding begins
        while (landsToGenerate > 0) {
            boolean successGenerate = false;
            while(!successGenerate){
                // Pick a random land already made
                Land randomLand = lands.get(rand.nextInt(lands.size()));
                // Use the method for generating available candidates for centers
                ArrayList<Coordinate> candidates = generatePossibleNeighboursFromCoord(randomLand.coords);
                if(candidates.size() > 0){
                    // Pick one at random
                    Coordinate randomPicked = candidates.get(rand.nextInt(candidates.size()));
                    // Add it to the list of coordinates we have already used
                    centersInUse.add(randomPicked);
                    // Also generate the actual land, giving it these coordiantes
                    Land newlyGenerated = new Land(14-landsToGenerate + "", 14-landsToGenerate, (landsToGenerate-7 > 0 ? players.get(0) : players.get(1)), randomPicked);
                    // Generate its neighbours while we have it
                    setNeighboursOfLand(newlyGenerated);
                    // Add this one after generating its neighbours (important order, do not change, see method setNeighboursOfLand)
                    lands.add(newlyGenerated);
                    successGenerate = true; // We found and chose a candidate
                } // If there are no candidates, we could not generate any candidates from this coordinate.
            }
            // This land is done, off to the next one
            landsToGenerate--;
        }

        for (Land land : lands) {
            System.out.println("(" + land.coords.x + ", " + land.coords.y + ")");
        }
    }


    private ArrayList<Coordinate> generatePossibleNeighboursFromCoord(Coordinate coord){
        ArrayList<Coordinate> candidates = new ArrayList<>();
        // See if either its center plus 3 or minus 3 in all combinations is in use
        // This is done by checking that no center lies 2 or less away in any coordinate
        // The large offset is used 4 times. X = -3, X = +3, Y = -3, Y = +3
        // The small offset is used 6 times. For both coordinates: -1, 0, +1
        // The idea here is that a coordinate set is used twice.
        // So we can use (-3,-1) in one round, but also (-1,-3).
        for (int large = -3; large < 4; large=large+6) { // This runs twice
            for (int small = -1; small < 2; small++) { // This runs 3 times
                // Now check (small, large) and (large, small), and add them
                Coordinate toCheck = new Coordinate(coord.x+large, coord.y+small);
                if(isCoordinateAvailable(toCheck)){ // Checking (large, small)
                    candidates.add(toCheck);
                }
                toCheck = new Coordinate(coord.x+small, coord.y+large);
                if(isCoordinateAvailable(toCheck)){ // Checking (small, large)
                    candidates.add(toCheck);
                }
            }
        }
        return candidates;
    }

    /*
     *  Checks if a that using this coordinate would cause collision
     *  Returns true if this coordinate can be used with no conflict
     */
    private boolean isCoordinateAvailable(Coordinate toCheck){
        boolean isAvailable = true;
        Iterator<Coordinate> coordIte = centersInUse.iterator(); // Make iterator
        while(coordIte.hasNext() && isAvailable){ // Run while we have not failed, and have more to check
            Coordinate currentToCheck = coordIte.next();
            boolean xConflict = false;
            boolean yConflict = false;
            // If this has its center in any of the 16 coordinates around the one we are checking, it fails.
            // What is being tested is 4 things. Does it lie further away by 3 or more on the x-coordinate, plus and minus, and then the same on the y-coordinate.
            // If the coordinate is not either on the outer side of any of these boundaries, that causes a conflict on that axis
            
            // Fx if currentToCheck.x > tocheck.x - 2, that means it lies on the right side of this left-side boundary.
            // This is only a problem if it does not then lie on the right of the right boundary.

            // Using logic, it can be determined that there needs to be both a conflict in the x and y coordinates.
            // If it has the same x-coordinate, that can be allowed, if the y-coordinate does not come anywhere near.
            if(!(currentToCheck.x < toCheck.x-2 || currentToCheck.x > toCheck.x+2)){
                xConflict = true;
            }
            if(!(currentToCheck.y < toCheck.y-2 || currentToCheck.y > toCheck.y+2)){
                yConflict = true;
            }
            if(xConflict && yConflict){
                // Both coordinates had a conflict, the coordinate we are checking is not available
                isAvailable = false; 
            }
        }
        return isAvailable;
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
            if(!(possibleNeighbour.coords.x == land.coords.x-3 || possibleNeighbour.coords.x == land.coords.x+3)){
                // Using the exact same technique as in isCoordinateAvailable(), we now want this specific collision
                // This checks that it does not lie further away than 2 in the y-coordinate in either positive or negative
                if(!(possibleNeighbour.coords.y < land.coords.y-2 || possibleNeighbour.coords.y > land.coords.y+2)){
                    bordering = true;
                }
            }
            if(!(possibleNeighbour.coords.y == land.coords.y-3 || possibleNeighbour.coords.y == land.coords.y+3)){
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
        // Does the specified move actually move troops?
        if(move.count < 1){
            return false;
        }
        // Is there enough troops in the from-land to move any from it?
        if(move.from.getTroopCount() < 2){
            return false;
        }
        // Does the current player control the source of the movement?
        if(move.from.getController() != move.player){
            return false;
        }
        
        // Does the current player control the target?
        if(move.player == move.to.getController()){
            // This is a movement
            // Are the two specified lands connected?
            if(!move.from.isConnectedTo(move.to)){
                return false;
            }
            // This movement is legal
        } else {
            // This is an attack
            // Are the two lands neighbours?
            if(!move.from.hasNeighboringLand(move.to)){
                return false;
            }
            // This attack is legal
        }

        return true;
    }



// - - - - - - - - - - - - - - - - - - Reinforcements 

    /*
     *  Method for checking legibility of a player placing reinforcements where they want to
     *  Returns false if the reinforcement is not legal
     *  TODO decide if the logic for all reinforcement checking should occur here, or if error-messages should be tailored, and thus be in the game logic
     */
    public boolean canReinforce(Player player, Reinforcement reinforcement){
        // An amount above 0 must be chosen
        if(reinforcement.count < 1){
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
        Math.max(count, getControlledLandsCount(player)/3);

        return count;
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
     *  Counts the amount of land a player controls
     */
    public int getControlledLandsCount(Player player){
        int count = 0;
        for (Land land : this.lands) {
            if(land.getController() == player){
                count++;
            }
        }
        return count;
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
                    if(i == 0 && j == 0){
                        // Middle
                        toSet = (char) (land.landID+97);
                    } else if(i == 0){
                        // Right and left middle
                        toSet = '|';
                    } else if(j == 0){
                        // Bottom and top middle
                        toSet = '-';
                    } else if(i == -1 && j == -1){
                        // Top left
                        // toSet = '\\';
                        toSet = '+';
                    } else if(i == 1 && j == -1){
                        // Bottom left
                        // toSet = '/';
                        toSet = '+';
                    } else if(i == -1 && j == 1){
                        // Top right
                        // toSet = '/';
                        toSet = '+';
                    } else if(i == 1 && j == 1){
                        // Bottom right
                        // toSet = '\\';
                        toSet = '+';
                    }
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

}
