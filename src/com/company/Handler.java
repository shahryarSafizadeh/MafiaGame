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
    private boolean canSpeak;
    private boolean canRecieve;
    private boolean isReady;
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
        try {
            while (true){
                //registering players
                if (!isReady){
                    register();
                    server.increaseReadyPlayers();
                    sendToCLient("Waiting for other players to join..." , this);
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
                            resetVoting();
                            this.mode =  Mode.NIGHT;
                        case NIGHT:
                            //night things
                            this.out.writeUTF("THIS IS NIGHT BITCH");
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

    public void startVoting() throws IOException {
        long start = System.currentTimeMillis();
        long end = start + 30*1000;
        String line;
        int bytes = 0;
        while (System.currentTimeMillis()<end){
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (submitVote(line)) {
                    sendToCLient("[GOD]:You successfully voted!" , this);
                    //changing vote ---------> remember to done
                } else {
                    sendToCLient("[GOD]:Wrong input!Try again." , this);
                }
            }
        }
    }

    public boolean voting()throws IOException{
        sendToCLient("[GOD]:Voting time!you have 30 seconds to vote a player who seems to be mafia!" , this);
        sendToCLient("[GOD]:This is list of players :\n" , this);
        //showing players list
        showPlayersList();
        sendToCLient("-for voting a player please just type the players name!" , this);
        //starting voting
        startVoting();
        sendToCLient("\n[GOD]:VOTING TIME'S UP!\n" , this);
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
//        String choice="";
        if (client==null)
            sendToCLient("[GOD]:Nobody leaves the game." , this);
        else{
            if (!(this.role instanceof Mayor) && this.isAlive){
                this.out.writeUTF("[GOD]:Waiting for the Mayor...");
                sendToCLient("[GOD]:Waiting for the Mayor..." , this);
                synchronized (this){
                    try {
                        this.wait();
                        return true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.role instanceof Mayor && this.isAlive){
                mayorAct(client);
            }else if (this.role instanceof Mayor && !this.isAlive){
                client.isAlive=false;
                client.canSpeak = false;
                sendToCLient("[GOD]:Mayor voted to kill you. if you wanna see the rest of game type 1" , client);
                String choice1 =client.in.readUTF();
                client.canRecieve = choice1.equals("1");
                sendToAll("GOD" , client.getName() + " died.");
            }
            synchronized (this) {
                for (Handler c : clients){
                    if (!(c.role instanceof Mayor) && c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }
        return false;
    }

    //act of mayor after the voting
    public void mayorAct(Handler client){
        String choice;
        try {
            sendToCLient("[GOD]:if you are agree to kill " + client.getName()+" type (yes) if not type (no)" , this);
            while (true) {
                choice = in.readUTF();
                if (choice.equals("yes")) {
                    client.isAlive = false;
                    client.canSpeak = false;
                    sendToCLient("[GOD]:Mayor voted to kill you. if you wanna see the rest of the game type 1." , client);
                    String choice1 = client.in.readUTF();
                    if (choice1.equals("1"))
                        client.canRecieve = true;
                    else
                        client.canRecieve = false;
                    sendToAll("GOD" , client.getName() + " died.");
                    break;
                } else if (choice.equals("no")) {
                    sendToCLient("[GOD]:Mayor voted to save you." , client);
                    sendToAll("GOD" ,"Mayor saved " + client.getName() );
                    break;
                } else {
                    sendToCLient("Wrong input" , this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //resting voting results
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
                sendToCLient(i +")" + c.getName() +" has " + c.getVotes() +" votes." , this);
                i++;
                if (c.getVotes()>0) {
                    sendToCLient("- These are the voters:" , this);
                    for (Handler voter : clients) {
                        if (voter.getVotesTo().getName().equals(c.getName())) {
                            sendToCLient("   -" + voter.getName() , this);
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
                sendToCLient(i + ")" + client.getName() , this);
                i++;
            }
        }
    }

    //free chat things
    public void freeChat()throws IOException {
        long start = System.currentTimeMillis();
        long end = start + 10 * 1000;
        String line;
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
                    sendToCLient("[GOD]:You are dead!" , this);
                }
            }
        }
        sendToCLient("\n[GOD]:DAY CHAT TIMES UP\n" , this);

    }


    //first night things
    public void firstNight() throws IOException {
        //telling everybody for starting first night --------> do it better
        sendToCLient("[GOD]:Hey everyone this is first night." , this);
        //telling mafia  ---------> do it better
        showMafias();
        //telling citizens -------> do it better
        if (this.getRole() instanceof Citizen){
            sendToCLient("[GOD]:Your role is " + this.getRole().name , this);
            if (this.getRole() instanceof Mayor){
                Handler doc = findPlayerByRole("Doctor");
                sendToCLient("[GOD]:" + doc.name + " is Doctor!" , this);
            }
        }
    }

    //showing mafias to each other
    public void showMafias() throws IOException {
        int i = 1;
        if (this.role instanceof Mafia){
            if (server.getGame().getMafiaCount()==1) {
                sendToCLient("You are " + this.getRole().name , this);
            } else {
                sendToCLient("You are " + this.getRole().name + " and the other Mafias are:" , this);
                for (Handler client : clients) {
                    if (client.role.isMafia && !client.name.equals(this.name)) {
                        sendToCLient(i + ")" + client.name + " is " + client.getRole().name , this);
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
        sendToCLient("You left the game! if you want to see the rest of chat press 1 else 2!" , this);
        while (true) {
            String command = in.readUTF();
            if (!command.isBlank() && command.equals("1")) {
                this.canRecieve = true;
                break;
            } else if (!command.isBlank() && command.equals("2")) {
                this.canRecieve = false;
                break;
            }
            sendToCLient("Wrong input." , this);
        }
        this.canSpeak = false;
        sendToAll("GOD" , this.name +" left the game!");
    }

    //for registering to the chat room
    public void register() throws IOException{
        while (true){
            sendToCLient("Enter your username:" , this);
            String name = in.readUTF();
            if (!names.contains(name) && !name.isBlank()){
                names.add(name);
                this.name = name;
                break;
            }else if (names.contains(name)){
                sendToCLient("This user is in the game!Please choose another name." , this);
            }
        }
        while (true){
            sendToCLient("Type (ready) to join the game!" , this);
            String command = in.readUTF();
            if (!command.isBlank() && command.equals("ready")) {
                this.canSpeak = true;
                this.canRecieve = true;
                this.isAlive = true;
                sendToAll("GOD" , name + " joined the game!");
                this.isReady = true;
                int index = clients.indexOf(this);
                this.role =server.getRoles().get(index);
                break;
            }else {
                sendToCLient("Wrong input." , this);
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

    public void sendToCLient(String msg , Handler client) throws IOException {
        client.out.writeUTF(msg);
        client.out.flush();
    }



}
