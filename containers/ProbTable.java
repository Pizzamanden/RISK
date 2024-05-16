package containers;
import java.util.ArrayList;

public class ProbTable {
    // Container class for quick lookup of probabilities for attack outcomes
    public static double att3def2;
    public static double att2def2;
    public static double att1def2;

    public static double att3def1;
    public static double att2def1;
    public static double att1def1;
    
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
    public static ArrayList<Outcome> calcOutcomeProbs(int attackers, int defenders){
        ArrayList<Outcome> outcomes = new ArrayList<>();

        return outcomes;
    }


    public class Outcome{
        public int attackersDying;
        public int defendersDying;
        public double probability;

    }


}
