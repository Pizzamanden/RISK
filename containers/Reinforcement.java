package containers;

import game.*;

public class Reinforcement {
    // Simple container class for holding multiple pieces of information in one return/list/call


    public Land land;
    public int count;


    public Reinforcement(Land land, int count){
        this.land = land;
        this.count = count;
    }
}
