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
    public abstract Move attack(Board board);

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
     *  So the total combinations become:
     *  - Each zone has an amount of troops
     *  - For each bordering land in this zone, there can be made any combination of troops
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
            for (Land land : zone) { // We do not use the method in board, as we only care about bordering lands in this specific zone
                if(land.hasEnemyNeighbour()){
                    borderLands.add(land);
                }
            }
            // For each of these bordering lands, we can divide up the troops in this zone in all permutations
            for (ArrayList<Land> arrayList : connectedZones) {
                // For each of these lands, we could move between 1 and all available troops in there
                for (int i = 1; i <= zoneTroopCount; i++) {
                    
                }
            }
            // This constitues leaving troops on the border, or performing all possible attacks on all enemy lands which border this zone
            // We could also attack, and even do a mix of attacking and leaving troops on the border
            // We are really only interested making one attack on an enemy land per zone
            // We really just need to generate a move for each possible attack, for each possible enemy land target
            HashSet<Land> enemyLandTargets = new HashSet<>();
            for (Land borderland : borderLands) {
                enemyLandTargets.addAll(borderland.getHostileNeighbours());
            }
            // Add all attacks from 1 troop up to Math.min(3, zoneTroopCount)
            for (Land target : enemyLandTargets) {
                // Find an allied neighbour to this land, to designate as the source of the attack
                Land source = target.getHostileNeighbours().get(0); // This is allowed, as there is always one neighbour which is hostile to this hostile land, us. Otherwise we would never have found this land to begin with
                for (int i = 1; i <= Math.min(3, zoneTroopCount); i++) {
                    moveList.add(new Move(this, source, target, i));
                }
            }

        }
        return moveList;
    }
}