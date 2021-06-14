package com.company;

public class Doctor extends Citizen{

    private int canHeal=1;

    public Doctor() {
        super("Doctor");
    }

    public int getCanHeal() {
        return canHeal;
    }

    public void setCanHeal(int canHeal) {
        this.canHeal = canHeal;
    }
}
