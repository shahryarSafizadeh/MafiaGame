package com.company;

public abstract class Mafia extends Role{

    protected boolean hasPower;

    public Mafia(String name){
        super(name);
        this.isMafia = true;
    }


}
