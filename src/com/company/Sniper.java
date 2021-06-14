package com.company;

public class Sniper extends Citizen{

    private int hasShot = 1;

    public Sniper(){
        super("Sniper");
    }

    public int getHasShot() {
        return hasShot;
    }

    public void setHasShot(int shot){
        this.hasShot = shot;
    }
}
