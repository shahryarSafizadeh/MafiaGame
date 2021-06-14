package com.company;

public class Psychologist extends Citizen{

    private int canSilent = 2;

    public Psychologist(){
        super("Psychologist");
    }

    public int getCanSilent() {
        return canSilent;
    }

    public void setCanSilent() {
        this.canSilent--;
    }
}
