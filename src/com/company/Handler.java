package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Handler implements Runnable{

    private Socket socket;
    private Server server;
    protected DataOutputStream out;
    private DataInputStream in;
    private String name;
    private Role role;
    private ArrayList<Handler> clients;
    private static ArrayList<String> names = names = new ArrayList<>();
    public boolean canSpeak;
    public boolean canRecieve;
    public boolean isReady;
    private Mode mode;
    private boolean hasRole = false;
    private int votes;
    private Handler votesTo;

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public Handler(Socket socket , ArrayList<Handler> clients , Server server){
//        this.mode =server.getGame().getMode();
        this.socket = socket;
        this.server = server;
        this.clients = clients;
        canRecieve = false;
        canSpeak = false;

        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line="";
        try {
            while (true){
                //registering players
                if (!isReady){
                    register();
                    server.increaseReadyPlayers();
                    out.writeUTF("Waiting for other players to join...");
                }

                if (server.areAllPlayersReady()) {
                    this.mode = this.server.getGame().getMode();
                    switch (this.mode) {
                        case FIRSTNIGHT:
                            //first night things
                            firstNight();
                            this.server.getGame().setMode(Mode.FREECHAT);
                            this.mode = Mode.FREECHAT;
                        case FREECHAT:
                            //freechat things
                            freeChat();
                            this.mode = Mode.VOTING;
                        case VOTING:
                            //voting things
                            voting();
                            this.mode =  Mode.NIGHT;
                        case NIGHT:
                            //night things
                            notifyAllClients("THIS IS NIGHT MODE BITCH!");
                            return;
                    }
                    return;
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Handler getVotesTo() {
        return votesTo;
    }

    public void setVotesTo(Handler votesTo) {
        this.votesTo = votesTo;
    }

    public void voting()throws IOException{
        this.out.writeUTF("[GOD]:Voting time!you have 30 seconds to vote a player who seems to be mafia!");
        this.out.flush();
        this.out.writeUTF("[GOD]:This is list of players :");
        this.out.flush();
        //showing players list
        showPlayersList();
        this.out.writeUTF("-for voting a player please just type the players name!");
        this.out.flush();
        //timing stuff
        long start = System.currentTimeMillis();
        long end = start + 30*1000;
        //voting things
        String line ="";
        int bytes = 0;
        while (System.currentTimeMillis()<end){
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (submitVote(line)) {
                    this.out.writeUTF("[GOD]:You successfully voted!");
                } else {
                    this.out.writeUTF("[GOD]:Wrong input!Try again.");
                }
            }
        }
        this.out.writeUTF("[GOD]:VOTING TIME'S UP!");
        this.out.flush();
        showVotes();
        //final voting stuff
    }

    public void showVotes()throws IOException{
        int i=1;
        for (Handler c : clients){
            if (c.canSpeak){
                this.out.writeUTF(i +")" + c.getName() +" has " + c.getVotes() +" votes.");
                i++;
                if (c.getVotes()>0) {
                    this.out.writeUTF("- These are the voters:");
                    for (Handler voter : clients) {
                        if (voter.getVotesTo().getName().equals(c.getName())) {
                            this.out.writeUTF("   -" + voter.getName());
                        }
                    }
                }
            }
        }
    }


    public boolean submitVote(String name){
        if (this.name.equals(name))
            return false;
        for (Handler client : clients){
            if (client.name.equals(name) && client.canSpeak){
                this.votesTo = client;
                client.votes++;
                return true;
            }
        }
        return false;
    }

    public void showPlayersList() throws IOException{
        int i = 1;
        for (Handler client : clients){
            this.out.writeUTF(i+")"+client.getName());
            this.out.flush();
            i++;
        }
    }

    public void freeChat()throws IOException {
        long start = server.getSystemTime();
        long end = start + 30 * 1000;
        String line = "";
        int bytes = 0;
        while (server.getSystemTime() < end) {
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (line.equals("quit")) {
                    quit();
                    return;
                }
                if (this.canSpeak)
                    sendToAll(this.name, line);
                else {
                    this.out.writeUTF("[GOD]:You are dead!");
                    this.out.flush();
                }
            }
        }
        this.out.writeUTF("[GOD]:TIMES UP");
        this.out.flush();
    }


    public void firstNight() throws IOException {
        //telling everybody for starting first night --------> do it better
        this.out.writeUTF("[GOD]:Hey everyone this is first night.");
        this.out.flush();
        //telling mafia  ---------> do it better
        showMafias();
        //telling citizens -------> do it better
        if (this.getRole() instanceof Citizen){
            this.out.writeUTF("[GOD]:Your role is " + this.getRole().name);
            this.out.flush();
            if (this.getRole() instanceof Mayor){
                Handler doc = findPlayer("Doctor");
                this.out.writeUTF("[GOD]:" + doc.name + " is Doctor!");
                this.out.flush();
            }
        }
    }

    public void showMafias() throws IOException {
        int i = 1;
        if (this.role instanceof Mafia){
            this.out.writeUTF("You are " + this.getRole().name + " and the other Mafias are:");
            this.out.flush();
            for (Handler client : clients){
                if (client.role.isMafia && !client.name.equals(this.name)){
                    this.out.writeUTF(i+")"+ client.name + " is " + client.getRole().name);
                    i++;
                }
            }
        }
    }

    //finding a player by its role
    public Handler findPlayer(String role){
        for (Handler client : clients){
            if (client.getRole().name.equals(role)){
                return client;
            }
        }
        return null;
    }


    //for quiting from the chat
    public void quit() throws IOException{
        this.out.writeUTF("You left the chat! if you want to see the rest of chat press 1 else 2!");
        this.out.flush();
        while (true) {
            String command = in.readUTF();
            if (!command.isBlank() && command.equals("1")) {
                this.canRecieve = true;
                break;
            } else if (!command.isBlank() && command.equals("2")) {
                this.canRecieve = false;
                break;
            }
            System.out.println("Wrong input!");
        }
        this.canSpeak = false;
        notifyAllClients(this.name +" left the chat!");
    }

    //for registering to the chat room
    public void register() throws IOException{
        while (true){
            out.writeUTF("enter your name:");
            String name = in.readUTF();
            if (!names.contains(name) && !name.isBlank()){
                names.add(name);
                this.name = name;
            }else if (names.contains(name)){
                out.writeUTF("This user is in the game!Please choose another name.");
                continue;
            }
            break;
        }
        while (true){
            out.writeUTF("type ready to join the chat!");
            String command = in.readUTF();
            if (!command.isBlank() && command.equals("ready")) {
                this.canSpeak = true;
                this.canRecieve = true;
                notifyAllClients(name + " joined the chat!");
                this.isReady = true;
                int index = clients.indexOf(this);
                this.role =server.getRoles().get(index);
                break;
            }else {
                out.writeUTF("Wrong input!");
                continue;
            }
        }
    }


    //sending a text to everyone
    public void sendToAll(String sender , String msg){
        for (Handler client : clients){
            try {
                if (client.canRecieve) {
                    client.out.writeUTF("["+ sender +  "] : " + msg);
                    client.out.flush();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //notifying every one
    public void notifyAllClients(String msg){
        for (Handler client : clients){
            try {
                if (client.canRecieve) {
                    client.out.writeUTF(msg);
                    client.out.flush();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


}
