package com.company;

public abstract class Role {

    protected String name;
    protected boolean isMafia;
    protected boolean canSpeak;
    protected boolean canRecieve;

    public Role(String name){
        this.name = name;
        this.canRecieve = false;
        this.canSpeak = false;
    }

}
