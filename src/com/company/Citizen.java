package com.company;

public abstract class Citizen extends Role{

    public Citizen(String name){
        super(name);
        this.isMafia = false;
    }

}
