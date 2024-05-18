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

}
