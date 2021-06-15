package com.company;

/**
 * abstract class for role
 * @author shahryarsz
 * @version 1.1
 */
public abstract class Role {
    /**
     * role fields
     */
    protected String name;
    protected boolean isMafia;

    /**
     * simple constructor
     * @param name role name
     */
    public Role(String name){
        this.name = name;
    }

}
