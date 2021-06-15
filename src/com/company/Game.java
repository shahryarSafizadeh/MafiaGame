package com.company;
import java.util.*;

/**
 * Game class for making roles and the game
 * @author shahryarsz
 * @version 1.1
 */
public class Game {
    /**
     * game fields
     */
    private ArrayList<Role> roles;
    private ArrayList<Handler> clients;
    private int playerCount;
    private int mafiaCount;
    private Mode mode;
    private Server server;

    /**
     * simple constructor for the game by giving the server
     * @param server
     */
    public Game(Server server) {
        this.server = server;
        this.roles = new ArrayList<>();
        this.clients = server.getClients();
        this.playerCount = server.getPlayerCount();
        this.mafiaCount = this.playerCount / 3;
        this.mode = Mode.FIRSTNIGHT;
    }

    /**
     * getting game mode
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * setting game mode
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * role maker for the game
     * @param playerCount players count
     * @return array list of making roles
     */
    public synchronized ArrayList<Role> roleMaker(int playerCount){
        int mc = 0;
        //making mafias
        while (mc<this.mafiaCount) {
            switch (mc){
                case 0:
                    Role role1 = new GodFather();
                    this.roles.add(role1);
                    mc++;
                    break;
                case 1:
                    Role role2 = new DoctorLecter();
                    this.roles.add(role2);
                    mc++;
                    break;
                case 2:
                    Role role3 = new SimpleMafia();
                    this.roles.add(role3);
                    mc++;
                    break;
            }
        }
        //making citizens
        int cc = 0;
        while (cc < this.playerCount - this.mafiaCount) {
            switch (cc) {
                case 0:
                    Role role1 = new Doctor();
                    this.roles.add(role1);
                    cc++;
                    break;
                case 1:
                    Role role2 = new Sniper();
                    this.roles.add(role2);
                    cc++;
                    break;
                case 2:
                    Role role3 = new Detective();
                    this.roles.add(role3);
                    cc++;
                    break;
                case 3:
                    Role role4 = new Mayor();
                    this.roles.add(role4);
                    cc++;
                    break;
                case 4:
                    Role role5 = new Psychologist();
                    this.roles.add(role5);
                    cc++;
                    break;
                case 5:
                    Role role6 = new Badkooft();
                    this.roles.add(role6);
                    cc++;
                    break;
                case 6:
                    Role role7 = new SimpleCitizen();
                    this.roles.add(role7);
                    cc++;
                    break;
            }
        }
        Collections.shuffle(this.roles);
        return this.roles;
    }

    public int getMafiaCount() {
        return mafiaCount;
    }
}
