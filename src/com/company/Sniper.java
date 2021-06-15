package com.company;

/**
 * class for sniper role
 * @author shahryarsz
 * @version 1.1
 */
public class Sniper extends Citizen{
    /**
     * sniper shots
     */
    private int hasShot = 1;

    /**
     * simpler constructor
     */
    public Sniper(){
        super("Sniper");
    }

    /**
     * getting sniper shots
     * @return shots
     */
    public int getHasShot() {
        return hasShot;
    }

    /**
     * setting sniper shots
     * @param shot
     */
    public void setHasShot(int shot){
        this.hasShot = shot;
    }
}
