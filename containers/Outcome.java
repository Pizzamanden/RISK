package containers;


public class Outcome{
    public int attackersDying;
    public int defendersDying;
    public float probability;

    public Outcome(int attackersDying, int defendersDying, float probability){
        this.attackersDying = attackersDying;
        this.defendersDying = defendersDying;
        this.probability = probability;
    }

    public void printProbAsPercentage(){
        System.out.print(String.format("%.0f%%", probability*100));
    }

    public static void printProbAsPercentage(float prob){
        System.out.print(String.format("%.0f%%", prob*100));
    }


    /*
     *  Compares if two outcomes are equal
     *  All the values must just be equal
     */
    public boolean equals(Object other){
        if(other == null)
			return false;
		if(other == this)
			return true;
		if(!(other instanceof Outcome))
			return false;
		Outcome oOutcome = (Outcome) other;
        return (oOutcome.attackersDying == this.attackersDying && oOutcome.defendersDying == this.defendersDying && oOutcome.probability == this.probability);
    }
}
