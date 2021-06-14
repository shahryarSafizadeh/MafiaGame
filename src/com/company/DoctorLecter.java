package com.company;

public class DoctorLecter extends Mafia{

    private int canHeal = 1;

    public DoctorLecter(){
        super("Doctor lecter");
        this.hasPower = false;
    }

    public int getCanHeal() {
        return canHeal;
    }

    public void setCanHeal(int canHeal) {
        this.canHeal = canHeal;
    }

}
