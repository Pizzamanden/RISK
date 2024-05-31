package players;

import containers.*;
import containers.TurnPlan.Action;
import game.*;
import java.util.*;

// SSAI stands for "Surface Scan AI", because it only looks at how the board is right now, and then finds an oppertunity.
public class SSAI extends AI{

    private int depth;
    private TurnPlan cTurnPlan;

    
    ArrayList<Outcome> attTwo = ProbTable.getOutcomes(3,2);
    ArrayList<Outcome> attOne = ProbTable.getOutcomes(3,1);


    public SSAI(int assignedNumber, int depth){
        super(assignedNumber);
        this.depth = depth;
        this.cTurnPlan = new TurnPlan();
    }

    /*
     *  Method for returning an attack on a given board
     *  Uses the current plan, or replans if the plan is off track
     */
    @Override
    public Move attack(Board board) {
        Action nextAction = cTurnPlan.nextAction();
        // If the plan is not empty, we might still be done with the attack phase
        if(!(board.equals(nextAction.currentBoard))){
            // Our last attack made has fucked up the timeline, replan!
            planTurn(board, 0);
            // This action should be good, since we just made it on the current board
            nextAction = cTurnPlan.nextAction();
        }
        // Either we are still on plan, or we just replanned.
        // In any case, we can move ahead with the nextAction
        return (Move) nextAction.action;
    }

    /*
     *  Method for returning a move on a given board
     *  Uses the current plan, or replans if the plan is off track
     */
    @Override
    public Move move(Board board) {
        Action nextAction = cTurnPlan.nextAction();
        // If the plan is not empty, we might still be done with the attack phase
        if(!(board.equals(nextAction.currentBoard))){
            // Our last move made has fucked up the timeline, replan!
            planTurn(board, 0);
            // This action should be good, since we just made it on the current board
            nextAction = cTurnPlan.nextAction();
        }
        // Either we are still on plan, or we just replanned.
        // In any case, we can move ahead with the nextAction
        return (Move) nextAction.action;
    }

    /*
     *  Implementation forced by AI class
     */
    @Override
    public int evaluateBoard(Board board) {
        int eval = 1;
        // Time to actually make an AI
        // Since this AI is aggressive, it wants to use AI tools, helper methods, but it needs to declare its own final evaluation.
        return eval;
    }

    /*
     *  Method for returning a reinforcement for a given board, with some specified reinforcements left 
     *  For this AI, being asked to reinforce constitutes planning the entire turn
     *  Therefore, when this is called, the plan should be empty
     *  The AI then makes a plan, which functions as a queue
     *  Each reinforcement is a remove() on this queue, taking the next order
     *  The reinforcements in the queue should transition over to being attacks/moves at the same time that Game stops calling reinforcement(), whihc occurs when we are out of troops to place
     */
    @Override
    public Reinforcement reinforce(Board board, int reinforceRemaining) {
        // If this is our first reinforcement, the TurnPlan should be empty
        System.out.println("AI is asked for a reinforcement.");
        if(cTurnPlan.isPlanEmpty()){
            // We only replan here if we havent planned before. We either call planTurn with the reinforcements we had in the beginning of our turn, or with 0
            planTurn(board, reinforceRemaining);
        }
        // Execute reinforcements from plan
        Action nextAction = cTurnPlan.nextAction();
        if(!(nextAction.action instanceof Reinforcement)){
            // Something went wrong!
        }
        // Just return the action as the reinforcement that it is
        // No need to check for board equality, since this bears no risk of failure
        return (Reinforcement) nextAction.action;
    }


    /*
     *  This method sets the current turn plan
     *  This should be called if either:
     *  - There is no current plan
     *  - The plan that exists is no longer possible on the current board
     */
    private void planTurn(Board board, int reinforcementsToPlace){
        cTurnPlan.wipe();
        Board planBoard = board.copy();
        if(reinforcementsToPlace > 0){
            int reinforceRemaining = reinforcementsToPlace;
            // First we must reinforce
            // A better AI would plan its entire turn, and reinforce using that plan.
            // This AI just chooses weak border lands and adds troops on them
            ArrayList<Land> borderLands = planBoard.getControlledBorderLands(this);
            ArrayList<Reinforcement> reinforcementsToMake = new ArrayList<>();
            // For each land, we have a possible reinforcement to make
            for (Land land : borderLands) {
                reinforcementsToMake.add(new Reinforcement(land, 0));
            }
            while (reinforceRemaining > 0) {
                // Sort the lands, such that the ones with the lowest amount is first in the list
                borderLands.sort((l1, l2) -> Integer.compare(l1.getTroopCount(), l2.getTroopCount()));
                // Now we add to all the lands which share the lowest troop count
                // Or until we run out of reinforcements to place
                int landIndex = 0;
                int leastTroopsNum = borderLands.get(0).getTroopCount();
                // This loop runs as long as:
                // We still have reinforcements
                // We are still in bounds of the array
                // The land we are looking at are part of the lands which are tied for the lowest count
                while (landIndex < borderLands.size() && borderLands.get(landIndex).getTroopCount() == leastTroopsNum && reinforceRemaining > 0) {
                    // Find the reinforcement, and increase the troops we want to reinforce by 1
                    boolean found = false;
                    int reinfIndex = 0;
                    while(!found){
                        if(reinforcementsToMake.get(reinfIndex).land.equals(borderLands.get(landIndex))){
                            found = true;
                            reinforcementsToMake.get(reinfIndex).count = reinforcementsToMake.get(reinfIndex).count + 1;
                        }
                        reinfIndex++;
                    }
                    borderLands.get(landIndex).changeTroopCount(1);
                    // Also specify that we have one less reinforcement to place
                    reinforceRemaining--;
                    landIndex++;
                }
            }
            // For all these reinforcements, add them to the plan
            // But, our reinforcements use fake lands. So we need the actual lands
            for (Reinforcement reinf : reinforcementsToMake) {
                if(reinf.count > 0){
                    // Do not worry about the board being different
                    System.out.println("Adding reinforcement plan of " + reinf.land.getName() + " with " + reinf.count + " troops.");
                    reinf.land = board.getLandByName(reinf.land.getName());
                    // Add this reinforcement to the plan
                    cTurnPlan.addAction(board, reinf);
                }
            }
            // We are done fortifying the wall
        }

        
        // Now we plan our attacks
        // First we make a new board, so we can know it is real, but also unchanged
        planBoard = board.copy();
        // We go over all enemy bordering lands, and check for targets
        HashSet<Land> targetList = new HashSet<>();
        for (Land friendlyBorderLand : planBoard.getControlledBorderLands(this)) {
            for (Land hostileNeighbour : friendlyBorderLand.getNeighbours(this,false)) {
                // This is a land which is a hostile neighbour of one of our bordering lands
                targetList.add(hostileNeighbour);
            }
        }

        ArrayList<Outcome> attTwo = ProbTable.getOutcomes(3,2);
        ArrayList<Outcome> attOne = ProbTable.getOutcomes(3,1);


        // Now we go in the other order
        for (Land target : targetList) {
            int defenderCount = target.getTroopCount();
            // Now we can evaluate if attacking from the friendly land into this one is a good idea
            ArrayList<Land> sourceList = target.getNeighbours(this, true); // Get all neighbours we own
            sourceList.sort((l1, l2) -> Integer.compare(l2.getTroopCount(), l1.getTroopCount())); // Sort it descending
            // For each of these source lands, we can sum up how effective they can attack
            // This is done in rounds. Each of these lands make their strongest available attack. We then sum up the count of each of these attacks using a dictionary
            // The goal is to end up with a dictionary where numbers starting from the defending count are represented as keys, and the probability of, after the current round, having that amount of troops left in the defending land as the value.
            /*
            
            If some land has 8 defending troops in total, and we attack with 3 troops, there should now exist a world where they lost 0, 1 and 2 troops.
            This means there is a probability of 8, 7 and 6 troops being on that land.

            In a second round, now having possibilities of 8,7 and 6 troops, we again attack with 3 troops.
            The result of 0 defenders dying is now applied to all outcomes. For them to still have 8 troops, we must have rolled 0 defenders dead two times.
            For 7, we must have rolled 0 once, then 1, or reverse.
            For 6, we could have rolled 0 then 2, or 1 then 1, or 2 then 0.
            There are now also worlds where there are only 5 or 4 defenders left.

            So each key in this dict is all permutations, where all the numbers in the permutation summed up, then subtracted from 8, is the key
            This can be made by taking the key one and two higher and copying its list

            So for key = 6 in round 2, we could have (0,2)(1,1)(2,0)
            This list is made from its parents!
            (0) comes from 8 in the previous round
            (1) comes from 7 in the previous round
            (2) comes from 6 in the previous round
            To make this round, we need to make 6 + SUM(list) = 8
            So (0) becomes (0,2), (1) becomes (1,1) and (2) becomes (2,0)

            Lets do one more round
            key = 6, round = 3 we could have (0,2,0)(1,1,0)(2,0,0)(0,0,2)(0,1,1)(1,0,1)
            (0,0) comes from 8 in the previous round
            (1,0) and (0,1) comes from 7 in the previous round
            (0,2) and (1,1) and (2,0) comes from 6 in the previous round
            To make this round, we follow the same formula:
            (0,0) => (0,0,2), (0,1) => (0,1,1), (1,0) => (1,0,1), (0,2) => (0,2,0), (1,1) => (1,1,0), and (2,0) = (2,0,0)
            
            How do we use these lists?
            Each tuple is multiplied together, since it represents these outcomes happening
            For each key, each resulting multiplication is summed.

            */
            HashMap<Integer, ArrayList<ArrayList<Outcome>>> map = new HashMap<>();

            while(sourceList.get(0).getTroopCount() > 3){ // We continue calculating as long as we can attack with 3 troops
                // Get a list of the current keys we are working with
                Set<Integer> keySet = map.keySet();
                // We now specify how low we go
                // If we had 8 as the smallest, we now need to go to 6
                int smallestKey = Math.max(0, Math.min(defenderCount, getSmallest(keySet)-2)); // The max key is always defenderCount

                // We also need a new map, such that we look in the old, and make into the new
                HashMap<Integer, ArrayList<ArrayList<Outcome>>> tempMap = new HashMap<>();
                
                // Use attTwo outcome
                for (int i = defenderCount; i > Math.max(0, smallestKey); i--) { // It goes backwards, starts from the highest key
                    // Make the list on this key
                    tempMap.put(i, new ArrayList<>());

                    // Also check if the previous map had this index initialized
                    if(map.get(i) == null){
                        // Initialize it, and add an empty outcome list
                        map.put(i, new ArrayList<>());
                        map.get(i).add(new ArrayList<>());
                    }
                    
                    addToNewMap(map, tempMap, i, 0);
                    
                    if(i + 1 <= defenderCount){
                        addToNewMap(map, tempMap, i+1, 1);
                    }

                    if(i + 2 <= defenderCount){
                        addToNewMap(map, tempMap, i+2, 2);
                    }
                    
                }

                // The remaining defender count of 0 could be reached by either fighting 2 defenders and killing both, or fighting 1 and killing them.
                if(smallestKey == 0){
                    // Here we use attTwo and attOne outcomes
                    addToNewMap(map, tempMap, 0, 2);
                    // and a manual block, because of
                    for (ArrayList<Outcome> list : map.get(1)) {
                        ArrayList<Outcome> newKeyList = new ArrayList<>();
                        for (Outcome outcome : list) {
                            newKeyList.add(outcome);
                        }
                        // Now add the new one
                        newKeyList.add(attOne.get(1)); // 0 is 2 dead defs, 1 is 1 dead def, 2 is 0 dead defs.
                        // We always add to i, since this is where the outcomes on this new list will take us
                        tempMap.get(0).add(newKeyList);
                    }

                }
                // Pretend we lost 2 troops here
                sourceList.get(0).changeTroopCount(-2);
                // Re-sort the list
                sourceList.sort((l1, l2) -> Integer.compare(l2.getTroopCount(), l1.getTroopCount()));
            }

            // So, now we can do something with this target?
            // Compute prob of 0 troops remaining
            System.out.println("\nAI is planning to attack " + target.getName() + ".");
            System.out.println("Target has " + target.getTroopCount() + " troops.");
            System.out.println("Troop count remaining : Probability");
            for (Map.Entry<Integer, ArrayList<ArrayList<Outcome>>> remain : map.entrySet()) {
                float prob = 0;
                // Now we are looking at some remaining troop count. Time to print the prob of this happening
                for (ArrayList<Outcome> outcomeTuple : remain.getValue()) {
                    // Now we have each list of attack outcomes that could lead to this outcome. We now multiply all these together, and add them to prob
                    float permProb = outcomeTuple.removeFirst().probability;
                    for (Outcome outcome : outcomeTuple) {
                        permProb = permProb * outcome.probability;
                    }
                    prob = prob + permProb;
                }
                System.out.println("Troop count remaining : " + remain.getKey() + ", Probability : ");
                Outcome.printProbAsPercentage(prob);
            }
        }
        // Nonsense returns, no attacks or moves
        cTurnPlan.addAction(board, null);
        cTurnPlan.addAction(board, null);
    }

    private void addToNewMap(HashMap<Integer, ArrayList<ArrayList<Outcome>>> oldMap, HashMap<Integer, ArrayList<ArrayList<Outcome>>> newMap, int key, int deadDefs){
        for (ArrayList<Outcome> list : oldMap.get(key+deadDefs)) {
            ArrayList<Outcome> newKeyList = new ArrayList<>();
            for (Outcome outcome : list) {
                newKeyList.add(outcome);
            }
            // Now add the new one
            newKeyList.add(attTwo.get(deadDefs)); // 0 is 2 dead defs, 1 is 1 dead def, 2 is 0 dead defs.
            // We always add to i, since this is where the outcomes on this new list will take us
            newMap.get(key).add(newKeyList);
        }
    }


    private int getSmallest(Set<Integer> set){
        int smallest = Integer.MAX_VALUE;
        for (Integer integer : set) {
            if(integer < smallest){
                smallest = integer;
            }
        }
        return smallest;
    }

    public class Tuple{
        public int defDying;
        public float prob;
    }
    
}


/*


// Add this key, but where 0 defenders died
                    for (ArrayList<Outcome> list : map.get(i)) {
                        ArrayList<Outcome> newKeyList = new ArrayList<>();
                        for (Outcome outcome : list) {
                            newKeyList.add(outcome);
                        }
                        // Now add the new one
                        newKeyList.add(attTwo.get(0));
                        // We always add to i, since this is where the outcomes on this new list will take us
                        tempMap.get(i).add(newKeyList);
                    }

                    if(i + 1 <= defenderCount){ // We can go one up, and not go beyond the highest key
                        // Take from the one where 1 less defender have died, and add onto all of them the outcome where 1 dies
                        for (ArrayList<Outcome> list : map.get(i+1)) {
                            ArrayList<Outcome> newKeyList = new ArrayList<>();
                            for (Outcome outcome : list) {
                                newKeyList.add(outcome);
                            }
                            // Now add the new one
                            newKeyList.add(attTwo.get(1));
                            // We always add to i, since this is where the outcomes on this new list will take us
                            tempMap.get(i).add(newKeyList);
                        }
                    }

                    if(i + 2 <= defenderCount){ // We can go two up, and not go beyond the highest key
                        // Take from the one where 2 less defender have died, and add onto all of them the outcome where 2 dies
                        for (ArrayList<Outcome> list : map.get(i+2)) {
                            ArrayList<Outcome> newKeyList = new ArrayList<>();
                            for (Outcome outcome : list) {
                                newKeyList.add(outcome);
                            }
                            // Now add the new one
                            newKeyList.add(attTwo.get(2));
                            // We always add to i, since this is where the outcomes on this new list will take us
                            tempMap.get(i).add(newKeyList);
                        }
                    }








 */