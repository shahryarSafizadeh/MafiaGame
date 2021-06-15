package com.company;
/**
 * class for psychologist role
 * @author shahryarsz
 * @version 1.0
 */
public class Psychologist extends Citizen{
    /**
     * number of can silent
     */
    private int canSilent = 2;

    /**
     * simple constructor
     */
    public Psychologist(){
        super("Psychologist");
    }

    /**
     * getting can silent
     * @return can silent number
     */
    public int getCanSilent() {
        return canSilent;
    }

    /**
     * setting can silent
     */
    public void setCanSilent() {
        this.canSilent--;
    }
}
