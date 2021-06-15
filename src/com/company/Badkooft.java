package com.company;

/**
 * class for badkooft role
 * @author shahryarsz
 * @version 1.0
 */
public class Badkooft extends Citizen{
    /**
     * fields are armor and power of asking
     */
    private int hasArmor = 1;
    private int canAsk = 2;
    private boolean hasAsked;

    /**
     * simple constructor
     */
    public Badkooft(){
        super("Badkooft");
    }


    /**
     * getting hasAsked
     * @return
     */
    public boolean isHasAsked() {
        return hasAsked;
    }

    /**
     * setting hasAsked
     * @param hasAsked
     */
    public void setHasAsked(boolean hasAsked) {
        this.hasAsked = hasAsked;
    }

    /**
     * getting armor
     * @return
     */
    public int getHasArmor() {
        return hasArmor;
    }

    /**
     * setting armor
     * @param hasArmor
     */
    public void setHasArmor(int hasArmor) {
        this.hasArmor = hasArmor;
    }

    /**
     * getting canAsk
     * @return
     */
    public int getCanAsk() {
        return canAsk;
    }

    /**
     * setting canAsk
     */
    public void setCanAsk() {
        this.canAsk--;
    }
}
