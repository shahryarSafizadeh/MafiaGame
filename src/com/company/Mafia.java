package com.company;

public abstract class Mafia extends Role{


    public Mafia(String name){
        super(name);
        this.isMafia = true;
    }

    @Override
    public abstract void act(Game game);
}
