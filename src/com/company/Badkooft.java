package com.company;

public class Badkooft extends Citizen{

    private int hasArmor = 1;
    private int canAsk = 2;
    private boolean hasAsked;

    public Badkooft(){
        super("Badkooft");
    }

    public void setCanAsk(int canAsk) {
        this.canAsk = canAsk;
    }

    public boolean isHasAsked() {
        return hasAsked;
    }

    public void setHasAsked(boolean hasAsked) {
        this.hasAsked = hasAsked;
    }

    public int getHasArmor() {
        return hasArmor;
    }

    public void setHasArmor(int hasArmor) {
        this.hasArmor = hasArmor;
    }

    public int getCanAsk() {
        return canAsk;
    }

    public void setCanAsk() {
        this.canAsk--;
    }
}
