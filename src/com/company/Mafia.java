package com.company;

/**
 * abstract class for mafia roles
 * @author shahryarsz
 * @version 1.1
 */
public abstract class Mafia extends Role{
    /**
     * field of kill power
     */
    protected boolean hasPower;

    /**
     * simple constructor
     * @param name
     */
    public Mafia(String name){
        super(name);
        this.isMafia = true;
    }


}
