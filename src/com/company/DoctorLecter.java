package com.company;

/**
 * class for doctor lecter role
 * @author shahryarsz
 * @version 1.1
 */
public class DoctorLecter extends Mafia{
    /**
     * number of self heal chance
     */
    private int canHeal = 1;

    /**
     * simple constructor
     */
    public DoctorLecter(){
        super("Doctor lecter");
        this.hasPower = false;
    }

    /**
     * getting canHeal
     * @return canHeal
     */
    public int getCanHeal() {
        return canHeal;
    }

    /**
     * setting canHeal
     * @param canHeal
     */
    public void setCanHeal(int canHeal) {
        this.canHeal = canHeal;
    }

}
