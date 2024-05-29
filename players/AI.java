package players;
/*
 *  This class is a container for shared AI methods. Fx if all AI wants to figure something out, or uses helper methods, this is where they can be defined.
 *  They can also be overridden later, if need be.
 */

import containers.*;
import game.*;
import java.util.*;

public abstract class AI extends Player{


    public AI(int assignedNumber) {
        super(assignedNumber);
    }

    /*
     *  Keep this abstract in this extension
     */
    public abstract Move move(Board board);

    /*
     *  Keep this abstract in this extension
     */
    public abstract Reinforcement reinforce(Board board, int reinforceRemaining);


    /*
     *  All AI should evaluate a board
     *  It does depend on what the AI tailored to do, so it is abstract here
     */
    public abstract int evaluateBoard(Board board);

    /*
     *  Returns a list of all possible moves that can be done on this board
     *  These moves are dependant on each connected zone of control
     *  For each of these zone, there is a pool of troops available, which can:
     *  - Attack an enemy land which neighbours that zone
     *  - Be placed on the border of land in the zone, which borders an enemy land
     */
    public ArrayList<Move> generateMoveList(Board board){
        ArrayList<Move> moveList = new ArrayList<>();
        // Start by getting all connected zones
        ArrayList<ArrayList<Land>> connectedZones = board.getConnectedLandZones(this);
        // For each of these zone, we have a pool of movable troops
        for (ArrayList<Land> zone : connectedZones) {
            // Since the zones contain all land, we can use it to feed getConnectedMovableTroopCount, but then we would have to remove the land that calls it.
            // Instead, since we essentially just add the originating land once too many, we can just subtract it once to restore correctness.
            int zoneTroopCount = zone.get(0).getConnectedMoveableTroopCount(zone) - (zone.get(0).getTroopCount() - 1);
            // With this troop count in a zone, we should now find the border.
            ArrayList<Land> borderLands = new ArrayList<>();
            for (Land land : zone) {
                if(land.hasEnemyNeighbour()){
                    borderLands.add(land);
                }
            }
            // For each of these bordering lands, we can divide up the troops in this zone in all permutations
            
            // This constitues leaving troops on the border
            // We could also attack, and even do a mix of attacking and leaving troops on the border
            // We are really only interested making one attack on an enemy land per zone
            // We really just need to generate a move for each possible attack, for each possible enemy land target


        }


        return moveList;
    }
}