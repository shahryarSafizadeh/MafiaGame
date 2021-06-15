package com.company;

/**
 * abstract class for Citizen role
 * @author shahryarsz
 * @version 1.1
 */
public abstract class Citizen extends Role{
    /**
     * simple constructor
     * @param name
     */
    public Citizen(String name){
        super(name);
        this.isMafia = false;
    }

}
