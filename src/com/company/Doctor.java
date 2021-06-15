package com.company;

/**
 * class for doctor role
 * @author shahryarsz
 * @version 1.0
 */
public class Doctor extends Citizen{
    /**
     * number of self heal chance for doctor
     */
    private int canHeal=1;

    /**
     * simple constructor
     */
    public Doctor() {
        super("Doctor");
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
