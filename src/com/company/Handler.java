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
    private boolean isAlive;

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
                            break;
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
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //showing the results
        showVotes();
        //final voting stuff
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Handler client = whoToLeave();
        String choice="";
        if (client==null)
            this.out.writeUTF("[GOD]:Nobody leaves the game.");
        else{
            if (!(this.role instanceof Mayor) && this.isAlive){
                this.out.writeUTF("[GOD]:Waiting for the Mayor...");
//                try {
//                    Thread.sleep(7000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            if (this.role instanceof Mayor && this.isAlive){
               mayorAct(client);
            }else if (this.role instanceof Mayor && !this.isAlive){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//        resetVoting();
    }

    public void mayorAct(Handler client){
        String choice="";
        try {
            this.out.writeUTF("[GOD]:if you are agree to kill " + client.getName()+" type yes if not type no");
            this.out.flush();
            choice = in.readUTF();
            if (choice.equals("yes")){
                client.isAlive = false;
                client.canSpeak = false;
                client.out.writeUTF("[GOD]:Mayor voted to kill you. if you wanna see the rest of game type 1");
                client.out.flush();
                String choice1 =client.in.readUTF();
                if (choice1.equals("1"))
                    client.canRecieve = true;
                else
                    client.canRecieve = false;
                notifyAllClients("[GOD]:"+client.getName()+" is death.");
            }else if (choice.equals("no")){
                client.out.writeUTF("[GOD]:Mayor voted to save you.");
                client.out.flush();
                client.canRecieve=false;
                notifyAllClients("[GOD]:Mayor saved "+client.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void resetVoting(){
        for (Handler client : clients){
            client.votes=0;
            client.votesTo = null;
        }
    }

    //findout who should leave the game by voting
    public Handler whoToLeave(){
        int max = findMaxVote();
        int count=0;
        for (Handler client:clients){
            if (client.getVotes()==max)
                count++;
        }
        if (count==1)
            return findPlayerByVote(max);
        else
            return null;
    }

    //finding a player by number of votes
    public Handler findPlayerByVote(int max){
        for (Handler client : clients){
            if (client.getVotes()==max)
                return client;
        }
        return null;
    }

    //finding max number of votes
    public int findMaxVote(){
        int max=0;
        for (Handler client : clients){
            if (client.getVotes()>max)
                max = client.getVotes();
        }
        return max;
    }

    //showing voting results
    public void showVotes()throws IOException{
        int i=1;
        for (Handler c : clients){
            if (c.isAlive){
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

    //submitting a vote
    public boolean submitVote(String name){
        if (this.name.equals(name))
            return false;
        for (Handler client : clients){
            if (client.name.equals(name) && client.isAlive){
                this.votesTo = client;
                client.votes++;
                return true;
            }
        }
        return false;
    }

    //showing players list
    public void showPlayersList() throws IOException{
        int i = 1;
        for (Handler client : clients){
            if (client.isAlive) {
                this.out.writeUTF(i + ")" + client.getName());
                this.out.flush();
                i++;
            }
        }
    }

    //free chat things
    public void freeChat()throws IOException {
        long start = System.currentTimeMillis();
        long end = start + 10 * 1000;
        String line = "";
        int bytes = 0;
        while (System.currentTimeMillis()< end) {
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


    //first night things
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
                Handler doc = findPlayerByRole("Doctor");
                this.out.writeUTF("[GOD]:" + doc.name + " is Doctor!");
                this.out.flush();
            }
        }
    }

    //showing mafias to each other
    public void showMafias() throws IOException {
        int i = 1;
        if (this.role instanceof Mafia){
            if (server.getGame().getMafiaCount()==1) {
                this.out.writeUTF("You are " + this.getRole().name);
                this.out.flush();
            } else {
                this.out.writeUTF("You are " + this.getRole().name + " and the other Mafias are:");
                this.out.flush();
                for (Handler client : clients) {
                    if (client.role.isMafia && !client.name.equals(this.name)) {
                        this.out.writeUTF(i + ")" + client.name + " is " + client.getRole().name);
                        this.out.flush();
                        i++;
                    }
                }
            }
        }
    }

    //finding a player by its role
    public Handler findPlayerByRole(String role){
        for (Handler client : clients){
            if (client.getRole().name.equals(role)){
                return client;
            }
        }
        return null;
    }


    //for quiting from the chat ------------> has very very things to do
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
                this.isAlive = true;
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
