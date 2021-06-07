package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game implements Runnable{

    private ArrayList<Role> roles;
//    private boolean isDay;
//    private boolean isVoting;
//    private boolean isFirstNight;
    private ArrayList<Handler> clients;
    private HashMap<Handler , Role> players;
    private int playerCount;
    private int mafiaCount;
    private Mode mode;


//    public boolean isDay() {
//        return isDay;
//    }
//
//    public boolean isVoting() {
//        return isVoting;
//    }
//
//
//    public boolean isFirstNight() {
//        return isFirstNight;
//    }

    public HashMap<Handler, Role> getPlayers() {
        return players;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Game(Server server){
        this.roles = new ArrayList<>();
        this.players = new HashMap<>();
        this.clients = server.getClients();
        this.playerCount = server.getPlayerCount();
        this.mafiaCount = this.playerCount/3;
//        this.isDay = false;
//        this.isVoting = false;
//        this.isFirstNight = true;
        this.mode = Mode.FIRSTNIGHT;

        //Entering keys : Handler
        for (Handler client : clients){
            players.keySet().add(client);
        }
        ArrayList<Role> roles = new ArrayList<>();
        int mc = 0;
        //making mafias
        while (true) {
            if (mc < mafiaCount) {
                switch (mc){
                    case 0:
                        Role role1 = new GodFather();
                        roles.add(role1);
                        mc++;
                        break;
                    case 1:
                        Role role2 = new DoctorLecter();
                        roles.add(role2);
                        mc++;
                        break;
                    case 2:
                        Role role3 = new SimpleMafia();
                        roles.add(role3);
                        mc++;
                        break;
                }
            }else
                break;
        }
        int cc = 0;
        while (true){
            if (cc < playerCount-mafiaCount){
                switch (cc){
                    case 0:
                        Role role1 = new Doctor();
                        roles.add(role1);
                        cc++;
                        break;
                    case 1:
                        Role role2 = new Sniper();
                        roles.add(role2);
                        cc++;
                        break;
                    case 2:
                        Role role3 = new Detective();
                        roles.add(role3);
                        cc++;
                        break;
                    case 3:
                        Role role4 = new Mayor();
                        roles.add(role4);
                        cc++;
                        break;
                    case 4:
                        Role role5 = new Psychologist();
                        roles.add(role5);
                        cc++;
                        break;
                    case 5:
                        Role role6 = new Badkooft();
                        roles.add(role6);
                        cc++;
                        break;
                    case 6:
                        Role role7 = new SimpleCitizen();
                        roles.add(role7);
                        cc++;
                        break;
                }
            }else
                break;
        }

        int[] nums = {1,2,3,4,5,6,7,8,9,10};
        Random random = new Random();
        int x;
        for (int i = 0 ; i<100 ; i++){
            x = random.nextInt(10);
            int tmp = nums[0];
            nums[0] = nums[x];
            nums[x] = tmp;
        }
        for (int i = 0 ; i<10 ; i++){
            players.values().add(roles.get(nums[i]));
        }

    }

    @Override
    public void run() {

    }

}
