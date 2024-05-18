package containers;
import java.util.*;
import game.*;

public class ProbTable {
    // Container class for quick lookup of probabilities for attack outcomes
    public static ArrayList<ArrayList<ArrayList<Outcome>>> outcomeTable;
    
    /*
    To calculate all these probabilities, we need the two sets of outcomes.
    For attackers, we need the probability of rolling fx 3 sixes, if they have one dice
    And the same for defenders

    After this, we would need to calculate all the matchups and their outcome.
    If the attacker rolls 3 sixes, then only in 1 matchup do they lose both troops (defender rolls 2 sixes).
    In two outcomes do they win 1 and lose 1 (defender rolls 1 six and 1 not-six)
    But this second one is multiple differing rolls.

    This is done by matching one specific roll, fx attacker rolling 3 sixes, and then grouping defender rolls in 2 boxes for each oppertunity
    So one box is 2 attacker wins, one is 1 attacker win, and one is 0 attacker wins.
    In each box, we must now take ALL defender rolls, and sort them in YES and NO scenarios.
    YES is chosen if this specific roll would result in this outcome, and NO if otherwise.

    Probabilities are proportional, but we can just sum up this very simple sorting to achive the correct result
    */


    /*
     *  Method for creating a list of outcomes depending on the input
     *  A call to this will return a list of the outcomes that can happen, along with the probability of this outcome being chosen
     *  The probability of these outcomes should VERY FUCKING MUCH sum up to 1
     *  This could also be a table in of itself
     */
    public static ArrayList<Outcome> getOutcomes(int attackers, int defenders){
        if(outcomeTable == null){ // Check if the table is initialized
            // Initialize the entire table
            outcomeTable = new ArrayList<>();
            // The lookup is used as: Select in the outer using defenders, select in the inner using attackers.
            // Generate all the dice roll sets we need
            ArrayList<ArrayList<ArrayList<Integer>>> diceRollSets = new ArrayList<>();
            // The first uses the method that does not take any list, because we do not have one yet
            diceRollSets.add(makeDiceSet(1)); // Set diceCountRemaining to 1, as we only want a dice count of 1
            diceRollSets.add(makeDiceSet(1,diceRollSets.get(0))); // The remaining count is still 1, but now we can use the previous list to make this one
            diceRollSets.add(makeDiceSet(1,diceRollSets.get(1)));
            // For all combinations of attackers and defenders, use these sets to generate a set of outcomes
            for (int def = 0; def < 2; def++) {
                // In each row of the table, initialize it
                outcomeTable.add(new ArrayList<>());
                for (int att = 0; att < 3; att++) {
                    // For each row, selected with the defender count, select the column using attacker count. Now set this index
                    // Using just .add(), as this can only happen in correct order.
                    outcomeTable.get(def).add(calcScenario(diceRollSets.get(att), diceRollSets.get(def)));
                }
                
            }
        }

        return outcomeTable.get(defenders-1).get(attackers-1);
    }

    private static ArrayList<Outcome> calcScenario(ArrayList<ArrayList<Integer>> attackerRollSet, ArrayList<ArrayList<Integer>> defenderRollSet){
        ArrayList<Outcome> outcomes = new ArrayList<>();
        // Make note if we compare once or twice
        boolean twoRolls = (attackerRollSet.get(0).size() > 1 && defenderRollSet.get(0).size() > 1);

        // Init the list of scores for this method call
        ArrayList<Integer> scores = new ArrayList<>();
        for (int i = 0; i < (twoRolls ? 3 : 2); i++) {
            scores.add(0);
        }
        int total = 0;
        // Now generate all rolls for this dice count for both attacker and defender

        // Using these two lists of rolls, go over all their outcomes
        for (ArrayList<Integer> attackerRolls : attackerRollSet) { // Get a set of attacker rolls
            for (ArrayList<Integer> defenderRolls : defenderRollSet) { // Get a set of defender rolls
                int attacksWon = 0; // Count how many times the attacker won
                // Sort both lists of rolls, in a descending order
                Collections.sort(attackerRolls, Collections.reverseOrder()); 
                Collections.sort(defenderRolls, Collections.reverseOrder());
                if(attackerRolls.get(0) > defenderRolls.get(0)){ // If the attacker rolled higher, they win one
                    attacksWon++;
                }
                if(twoRolls){ // If there is two comparisons, compare second highest pair
                    if(attackerRolls.get(1) > defenderRolls.get(1)){ // If the attacker rolled higher, they win one
                        attacksWon++;
                    }
                }
                // Add one to the outcome.
                // Note that attacksWon can only sum to 2 if two comparisons were made, so without that, only index 0 and 1 are in use
                scores.set(attacksWon, scores.get(attacksWon)+1);
                total++;
            }
        }
        
        // Now calculate the outcomes
        // System.out.println("\nFor " + attackerRollSet.get(0).size() + " attackers and " + defenderRollSet.get(0).size() + " defenders:");
        // System.out.println("Total: " + total + "\n");
        for (int i = 0; i < scores.size(); i++) {
            outcomes.add(new Outcome((twoRolls ? 2 : 1)-i,i,(float) scores.get(i)/total));
            // System.out.println(((twoRolls ? 2 : 1)-i) + " A : " + i + " D");
            // System.out.println("Score: " + scores.get(i));
            // System.ou<cat.println("Prob: " + (float) scores.get(i)/total + "\n");
            // System.out.println(String.format("%.0f%%",(float) scores.get(i)/total*100));
        }

        return outcomes;
    }



    /*
     *  Recursive call for making dice roll combinations
     *  Use this method if a list already exists, then specify the desired extra added amount as the diceCountRemaining
     *  The method is not destructive in any way on the list provided as argument
     */
    private static ArrayList<ArrayList<Integer>> makeDiceSet(int diceCountRemaining, ArrayList<ArrayList<Integer>> currentList){
        if(diceCountRemaining == 0){ // Stop the recursion if there are no more dice to add
            return currentList;
        } else {
            // Make an entirely new list
            ArrayList<ArrayList<Integer>> newList = new ArrayList<>();
            for (ArrayList<Integer> arrayList : currentList) { // For each list of current dice rolls in the parameter, we want to make 6 new ones
                for (int i = 1; i < 7; i++) { // Here we make the 6 new ones
                    ArrayList<Integer> newPerm = (ArrayList<Integer>) arrayList.clone(); // Clone the current version, fx {1,5}
                    newPerm.add(i); // Now add this index, fx i = 3 would be {1,5,3}
                    newList.add(newPerm); // Add this to the new list
                }
            }
            return makeDiceSet(diceCountRemaining-1, newList);
        }
    }

    /*
     *  Initial usage of dice roll generation
     *  Use this if no list exists, to generate a list of the desired size.
     */
    public static ArrayList<ArrayList<Integer>> makeDiceSet(int diceCountRemaining){
        ArrayList<ArrayList<Integer>> newList = new ArrayList<>();
        for (int i = 1; i < 7; i++) { // Here we make the 6 new ones
            ArrayList<Integer> newPerm = new ArrayList<>();
            newPerm.add(i);
            newList.add(newPerm);
        }
        return makeDiceSet(diceCountRemaining-1, newList);
    }


    


}
