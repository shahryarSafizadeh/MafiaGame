package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 * client handler class for handling all the clients including game modes : day , voting , night
 * @author shahryarsz
 * @version 1.1
 */
public class Handler implements Runnable{
    /**
     * fields for each handler(player)
     */
    private Socket socket;
    private Server server;
    protected DataOutputStream out;
    protected DataInputStream in;
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
    public int getShot;
    private boolean newDead;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * getting handler name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * getting handler role
     * @return role
     */
    public Role getRole() {
        return role;
    }

    /**
     * creating a handler
     * @param socket client socket
     * @param clients client list
     * @param server server socket
     */
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

    /**
     * overriding run method for handling client jobs
     */
    @Override
    public void run() {
        try {
            while (true){
                //registering players
                if (!isReady){
                    register();
                    server.increaseReadyPlayers();
                    sendToCLient(colorStr("[GOD]:Waiting for other players to join..." , "purple") , this);
                }
                //running chat modes
                if (server.areAllPlayersReady()) {
                    this.mode = this.server.getGame().getMode();
                    switch (this.mode) {
                        case FIRSTNIGHT:
                            //first night things
                            firstNight();
                            this.server.getGame().setMode(Mode.FREECHAT);
//                            this.wait(1000);
                            this.mode = Mode.FREECHAT;
                        case FREECHAT:
                            //free chat things
                            freeChat();
                            everyOneCanSpeak();
//                            this.wait(1000);
                            this.mode = Mode.VOTING;
                        case VOTING:
                            //voting things
                            voting();
                            resetVoting();
//                            this.wait(1000);
                            this.mode =  Mode.NIGHT;
                        case NIGHT:
                            //night things
                            night();
                            resetNewDead();
//                            this.wait(1000);
                            if (checkEnd())
                                return;
                            this.mode = Mode.FREECHAT;
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * method for night actions of each role
     * @throws IOException
     * @throws InterruptedException
     */
    public  void night() throws IOException, InterruptedException {
        sendToCLient(colorStr("[GOD]:Its night . Please close your eyes." , "cyan"), this);

        //mafia thing
        if (this.role instanceof Citizen && this.isAlive){
            sendToCLient(colorStr("[GOD]:Waking up Mafia team..." , "blue") , this);
            synchronized (this){
                this.wait();
            }
        }

        if (this.role instanceof Mafia && this.isAlive){
            mafiaAct();
            synchronized (this) {
                for (Handler c : clients){
                    if (c.isAlive) {
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }

        //lecter thing
        if (gameHasRole("Doctor lecter")) {
            if (!(this.role instanceof DoctorLecter) && this.isAlive){
                sendToCLient(colorStr("[GOD]:Waking up Doctor Lecter..." , "blue") , this);
                synchronized (this){
                    this.wait();
                }
            }
            if (this.role instanceof DoctorLecter){
                if (this.isAlive) {
                    lecterAct();
                }
                else {
                    Thread.sleep(5000);
                }
                synchronized (this) {
                    for (Handler c : clients){
                        if (c.isAlive){
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }

            }
        }

        //doctor thing
        if (!(this.role instanceof Doctor) && this.isAlive){
            sendToCLient(colorStr("[GOD]:Waking up Doctor..." , "blue") , this);
            synchronized (this){
                this.wait();
            }
        }
        if (this.role instanceof Doctor){
            if (this.isAlive) {
                doctorAct();
            } else {
                Thread.sleep(5000);
            }
            synchronized (this) {
                for (Handler c : clients){
                    if (c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }


        //detective thing
        if (!(this.role instanceof Detective) && this.isAlive){
            sendToCLient(colorStr("[GOD]:Waking up Detective..." , "blue") , this);
            synchronized (this){
                this.wait();
            }
        }
        if (this.role instanceof Detective){
            if (this.isAlive) {
                detectiveAct();
            } else {
                Thread.sleep(5000);
            }
            synchronized (this) {
                for (Handler c : clients){
                    if ( c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }

        //sniper thing
        if (!(this.role instanceof Sniper) && this.isAlive){
            sendToCLient(colorStr("[GOD]:Waking up Sniper..." , "blue") , this);
            synchronized (this){
                this.wait();
            }
        }
        if (this.role instanceof Sniper) {
            if (this.isAlive)
                sniperAct();
            else {
                Thread.sleep(5000);
            }
            synchronized (this) {
                for (Handler c : clients){
                    if (c.isAlive){
                        synchronized (c) {
                            c.notify();
                        }
                    }
                }
            }
        }

        //Psychologist things
        if (gameHasRole("Psychologist")){
            if (!(this.role instanceof Psychologist) && this.isAlive){
                sendToCLient(colorStr("[GOD]:Waking up Psychologist..." , "blue") , this);
                synchronized (this){
                    this.wait();
                }
            }
            if (this.role instanceof Psychologist){
                if (this.isAlive)
                    psychologistAct();
                else
                    Thread.sleep(5000);

                synchronized (this) {
                    for (Handler c : clients){
                        if (c.isAlive){
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }
            }

        }

        //badkooft things
        if (gameHasRole("Badkooft")){
            if (!(this.role instanceof Badkooft) && this.isAlive){
                sendToCLient(colorStr("[GOD]:Waking up Badkooft..."  , "blue"), this);
                synchronized (this){
                    this.wait();
                }
            }
            if (this.role instanceof Badkooft){
                if (this.isAlive)
                    badkooftAct();
                else
                    Thread.sleep(5000);

                synchronized (this) {
                    for (Handler c : clients){
                        if ( c.isAlive){
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }
            }
        }
        int countDead;
        //nobody dies
        if (isEveryOneAlive()){
            sendToCLient(colorStr("[GOD]:Calculating last night things..." , "blue") , this);
            Thread.sleep(1000);
            nightAnnounce();
            if (gameHasRole("Badkooft")) {
                if (this.role instanceof Badkooft && this.isAlive){
                    if (((Badkooft) this.role).isHasAsked()){
                        sendToCLient(colorStr("\n[GOD]:Badkooft has asked for the dead players roles.\n" , "white") , this);
                        badkooftAnnounce();
                        ((Badkooft) this.role).setHasAsked(false);
                    }
                }
            }
        }else { // we have dead player
            if (this.isAlive && this.getShot<1){
                sendToCLient(colorStr("[GOD]:Calculating last night things..." , "blue") , this);
                synchronized (this){
                    this.wait();
                }
            }
            countDead=0;
            for (Handler client : clients) {
                if (client.isAlive && client.getShot > 0) {
                    client.newDead=true;
                    sendToCLient(colorStr("[GOD]:You have been killed last night , " +
                            "for watching the rest of the game type 1 else 2." , "cyan"), client);
                    String choice = client.in.readUTF();
                    client.canRecieve = choice.equals("1");
                    client.isAlive = false;
                    countDead++;
                }
            }
            if (countDead==calculateNewDead()) {
                synchronized (this) {
                    for (Handler c : clients) {
                        if (c.isAlive) {
                            synchronized (c) {
                                c.notify();
                            }
                        }
                    }
                }
            }

            nightAnnounce();

            if (gameHasRole("Badkooft")) {
                if (this.role instanceof Badkooft && this.isAlive){
                    if (((Badkooft) this.role).isHasAsked()){
                        sendToCLient(colorStr("\n[GOD]:Badkooft has asked for the dead players roles.\n" , "white") , this);
                        badkooftAnnounce();
                        ((Badkooft) this.role).setHasAsked(false);
                    }
                }
            }
        }
    }

    /**
     * check if no one died last night
     * @return if no one died : true else false
     */
    public boolean isEveryOneAlive(){
        for (Handler client :clients){
            if (client.isAlive && client.getShot > 0){
                return false;
            }
        }
        return true;
    }

    /**
     * getting number of alive players
     * @return alive players amount
     */
    public int getAllAlive() {
        int count=0;
        for (Handler c : clients){
            if (c.isAlive)
                count++;
        }
        return count;
    }

    /**
     * checking if the game has finished or not
     * @return if mafias has finished or not
     * @throws IOException
     */
    public boolean checkEnd()throws IOException{
        int mafias = 0 , citizens = 0;
        for (Handler client : clients){
            if (client.isAlive){
                if (client.role.isMafia)
                    mafias++;
                else
                    citizens++;
            }
        }

        if (mafias>=citizens && this.canRecieve){
            sendToCLient(colorStr("[GOD]:Game is over! MAFIA WON!!!" , "purple"), this);
            return true;
        }else if (mafias==0 && this.canRecieve){
            sendToCLient(colorStr("[GOD]:Game is over! CITIZEN WON!!!"  , "purple"), this);
            return true;
        }
        return false;
    }

    /**
     * special announcement of badkooft role
     * @throws IOException
     */
    public void badkooftAnnounce() throws IOException {
        ArrayList<Handler> deadPlayers = new ArrayList<>();
        for (Handler client : clients){
            if (!client.isAlive){
                deadPlayers.add(client);
            }
        }
        Collections.shuffle(deadPlayers);
        sendToCLient(colorStr("[GOD]:This roles aren't in the game anymore:\n" ,"white") , this);
        int i=1;
        for (Handler client : deadPlayers){
            sendToCLient(colorStr("  "+i+")"+client.getRole().name , "white") , this);
            i++;
        }
    }

    /**
     * resetting newDead players
     */
    public void resetNewDead(){
        for (Handler client : clients){
            client.newDead=false;
        }
    }

    /**
     * announcement after ending every nights
     * @throws IOException
     */
    public synchronized void nightAnnounce()throws IOException{
        sendToCLient(colorStr("\n[GOD]:Last night this things happened:\n" , "white") , this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (calculateNewDead()>0){
            sendToCLient(colorStr("\n[GOD]:Last night this things happened:\n" , "white") , this);
            int i=1;
            for (Handler client : clients){
                if (client.newDead) {
                    sendToCLient(colorStr("      " + i + ")" + client.name , "white"), this);
                    i++;
                }
            }
        }else {
            sendToCLient(colorStr("[GOD]:Nobody died last night." , "white") , this);
        }

        for (Handler client : clients){
            if (client.isAlive && !client.canSpeak){
                sendToCLient(colorStr("  -[GOD]:And " + client.getName() + " is silent today." , "white") , this);
            }
        }

    }

    /**
     * calculating newDead players
     * @return newDead players number
     */
    public int calculateNewDead(){
        int count=0;
        for (Handler cleint : clients){
            if (cleint.newDead)
                count++;
        }
        return count;
    }

    /**
     * checking if game has an special role or not
     * @param roleName role name
     * @return if it has : true else : false
     */
    public boolean gameHasRole(String roleName){
        for (Handler client : clients){
            if (client.role.name.equals(roleName))
                return true;

        }
        return false;
    }

    /**
     * let everyone speak
     */
    public void everyOneCanSpeak(){
        for (Handler client : clients){
            if (client.isAlive){
                client.canSpeak = true;
            }
        }
    }

    /**
     * badkooft actions at night
     * @throws IOException
     */
    public void badkooftAct()throws IOException{
        if (((Badkooft)this.role).getCanAsk()!=0){
            sendToCLient(colorStr("[GOD]:if you want to ask what happened at night type 1." , "cyan") , this);
            String choice = in.readUTF();
            if (choice.equals("1")){
                ((Badkooft) this.role).setCanAsk();
                ((Badkooft) this.role).setHasAsked(true);
            }
        }else {
            sendToCLient(colorStr("[GOD]:You cant ask about night things anymore!" , "cyan") , this);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * psychologist actions at night
     * @throws IOException
     */
    public void psychologistAct() throws IOException{
        if (((Psychologist)this.role).getCanSilent()!=0){
            sendToCLient(colorStr("[GOD]:You can silent a player if you want to type 1." , "cyan") , this);
            String choice = in.readUTF();
            if (choice.equals("1")){
                while (true){
                    sendToCLient(colorStr("[GOD]:Which player you want to silent?" , "cyan") , this);
                    String name = in.readUTF();
                    Handler player = findPlayerByName(name);
                    if ( player!=null && player.isAlive){
                        player.canSpeak = false;
                        ((Psychologist) this.role).setCanSilent();
                        break;
                    }else {
                        sendToCLient(colorStr("[GOD]:Wrong input." ,"red"), this);
                    }
                }
            }
        }else {
            sendToCLient(colorStr("[GOD]:You cant silent anymore!" , "cyan") , this);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * sniper actions at night
     * @throws IOException
     */
    public void sniperAct() throws IOException{
        if (((Sniper)this.role).getHasShot()!= 0){
            sendToCLient(colorStr("[GOD]:You have one shot , if you want to take your shot type 1." , "cyan") , this);
            String choice =  in.readUTF();
            if (choice.equals("1")){
                while (true){
                    sendToCLient(colorStr("[GOD]:Which player you want to shoot?" , "cyan") , this);
                    String name = in.readUTF();
                    Handler player = findPlayerByName(name);
                    if (player!=null && player.isAlive && !player.name.equals(this.name)){
                        if (player.role instanceof Citizen){
                            sendToCLient(colorStr("[GOD]:You shot one of citizens and now you are dead." +
                                    "if you want see the rest of the game type 1 else 2." , "cyan") , this);
                            String choice1 = in.readUTF();
                            this.canRecieve = choice1.equals("1");
                            this.isAlive = false;
                            break;
                        }else {
                            shot(player);
                            ((Sniper) this.role).setHasShot(0);
                            break;
                        }
                    }else {
                        sendToCLient(colorStr("[GOD]:Wrong input." , "red") , this);
                    }
                }
            }
        }else {
            sendToCLient(colorStr("[GOD]:Wrong input." , "cyan") , this);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * detective actions at night
     * @throws IOException
     */
    public void detectiveAct() throws IOException {
        sendToCLient(colorStr("[GOD]:Choose a player and I tell you if its mafia or not!" , "cyan") , this);
        String choice;
        while (true){
            choice = in.readUTF();
            if (findPlayerByName(choice) == null || findPlayerByName(choice).name.equals(this.name)){
                sendToCLient(colorStr("[GOD]:Wrong input." , "red") , this);
            }else if (findPlayerByName(choice).role instanceof Citizen || findPlayerByName(choice).role instanceof GodFather){
                sendToCLient(colorStr("[GOD]:This player is not mafia." , "blue") , this);
                break;
            }else if (findPlayerByName(choice).role instanceof Mafia){
                sendToCLient(colorStr("[GOD]:This player is mafia." , "red") , this);
                break;
            }
        }
    }

    /**
     * doctor lecter actions at night
     * @throws IOException
     */
    public void lecterAct() throws IOException {
        while (true) {
            sendToCLient(colorStr("[GOD]:Which mafia you want to heal?" , "cyan"), this);
            String name = in.readUTF();
            Handler player = findPlayerByName(name);
            if (player!=null && player.role instanceof DoctorLecter){
                if (((DoctorLecter) player.role).getCanHeal()!=0){
                    player.getShot--;
                    ((DoctorLecter) this.role).setCanHeal(0);
                    break;
                }else {
                    sendToCLient(colorStr("[GOD]:Which mafia you want to heal?" , "cyan") , this);
                }
            }else if (player!=null && player.role.isMafia && player.isAlive){
                player.getShot--;
                ((DoctorLecter) this.role).setCanHeal(0);
                break;
            }else {
                sendToCLient(colorStr("[GOD]:Wrong input!" , "red") , this);
            }
        }
    }

    /**
     * doctor actions at night
     * @throws IOException
     */
    public void doctorAct()throws IOException{
        while (true) {
            sendToCLient(colorStr("[GOD]:Which player you want to heal?" , "cyan"), this);
            String name = in.readUTF();
            Handler player = findPlayerByName(name);
            if (player!=null && player.role instanceof Doctor){
                if (((Doctor) player.role).getCanHeal()!=0){
                    player.getShot--;
                    ((Doctor) this.role).setCanHeal(0);
                    break;
                }else {
                    sendToCLient(colorStr("[GOD]:You cant heal your self anymore." , "cyan") , this);
                }
            }else if (player!=null && player.isAlive){
                player.getShot--;
                ((Doctor) this.role).setCanHeal(0);
                break;
            }else {
                sendToCLient(colorStr("[GOD]:Wrong input!" , "red") , this);
            }
        }
    }

    /**
     * mafia actions at night
     * @throws IOException
     */
    public void mafiaAct() throws IOException {
        //Citizens sleeping
        for (Handler client : clients){
            if (client.role instanceof Citizen && client.isAlive){
                client.canSpeak = false;
                client.canRecieve = false;
            }
        }
        //announcing them and creating their chatroom for only 30 seconds
        sendToCLient(colorStr("[GOD]:You have 30 seconds for consulting!" , "cyan") , this);
        long start = System.currentTimeMillis();
//        long end = start + 30*1000;
        long end = start + 7 * 1000;
        String line;
        int bytes = 0;
        while (System.currentTimeMillis()< end) {
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (this.canSpeak)
                    sendToAll(this.name, line);
                else {
                    sendToCLient(colorStr("[GOD]:You have 30 seconds for consulting!" , "cyan") , this);
                }
            }
        }
        sendToCLient(colorStr("[GOD]:TIMES UP!" , "cyan") , this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String mafiasChoice;
        setMafiasPower();
        if (this.role instanceof Mafia){
            if (((Mafia) this.role).hasPower){
                sendToCLient(colorStr("[GOD]:You should type a player name to shoot." , "cyan") , this);
                while (true) {
                    mafiasChoice = in.readUTF();
                    Handler client = findPlayerByName(mafiasChoice);
                    if (client!=null && client.isAlive){
                        if (client.role instanceof Badkooft){
                            if (((Badkooft) client.role).getHasArmor()>0) {
                                ((Badkooft) client.role).setHasArmor(0);
                                break;
                            }
                        }
                        shot(client);
                        break;
                    }else {
                        sendToCLient(colorStr("[GOD]:Wrong input!" , "red") , this);
                    }
                }
            } else {
                synchronized (this){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (Handler client : clients){
            if (client.role instanceof Citizen && client.isAlive){
                client.canSpeak = true;
                client.canRecieve = true;
            }
        }

    }

    /**
     * giving the killing power to the head mafia
     */
    public synchronized void setMafiasPower(){
        Handler godFather = findPlayerByRole("God father");
        Handler doctorLecter = findPlayerByRole("Doctor lecter");
        Handler simpleMafia = findPlayerByRole("Havij Mafia");
        if ( godFather!=null && godFather.isAlive ){
            ((Mafia)godFather.role).hasPower = true;
        }else if ( doctorLecter!=null && doctorLecter.isAlive){
            ((Mafia)doctorLecter.role).hasPower = true;
        }else if (simpleMafia!=null && simpleMafia.isAlive){
            ((Mafia)simpleMafia.role).hasPower = true;
        }
    }

    /**
     * shooting a player a player
     * @param client player
     * @throws IOException
     */
    public void shot(Handler client) throws IOException {
        client.getShot++;
    }

    /**
     * finding player by name
     * @param name player name
     * @return player
     */
    public Handler findPlayerByName(String name){
        for (Handler client : clients){
            if (client.name.equals(name) && client.isAlive){
                return client;
            }
        }
        return null;
    }

    /**
     * getting votes of a player
     * @return player votes
     */
    public int getVotes() {
        return votes;
    }

    /**
     * setting player votes
     * @param votes
     */
    public void setVotes(int votes) {
        this.votes = votes;
    }

    /**
     * getting a player who votes to this player
     * @return voter
     */
    public Handler getVotesTo() {
        return votesTo;
    }

    /**
     * setting a voter to a player
     * @param votesTo voter
     */
    public void setVotesTo(Handler votesTo) {
        this.votesTo = votesTo;
    }

    /**
     * start voting : sending and submitting
     * @throws IOException
     */
    public  void startVoting() throws IOException {
        long start = System.currentTimeMillis();
//        long end = start + 30*1000;
        long end = start + 15*1000;
        String line;
        int bytes = 0;
        while (System.currentTimeMillis()<end){
            bytes = in.available();
            if (bytes>0) {
                line = in.readUTF();
                System.out.println(line);
                if (canVote(line)) {
                    if (line.equals("0"))
                        sendToCLient(colorStr("[GOD]:You vote nobody!" , "cyan") , this);
                    else {
                        sendToCLient(colorStr("[GOD]:You successfully voted!" , "cyan"), this);
                        submitVote(line);
                    }
                } else {
                    sendToCLient(colorStr("[GOD]:Wrong input!Try again." , "red"), this);
                }
            }
        }
    }

    /**
     * voting all actions for each client
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean voting() throws IOException, InterruptedException {
        sendToCLient(colorStr("\n[GOD]:Voting time!you have 30 seconds to vote a player who seems to be mafia!\n" , "cyan") , this);
        sendToCLient(colorStr("[GOD]:This is list of players :\n" , "cyan") , this);
        //showing players list
        showPlayersList();
        sendToCLient(colorStr("-for voting a player please just type the players name!" , "cyan") , this);
        //starting voting
        startVoting();
        sendToCLient(colorStr("\n[GOD]:VOTING TIME'S UP!\n" , "cyan") , this);
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
        if (client==null)
            sendToCLient(colorStr("[GOD]:Nobody leaves the game." , "cyan") , this);
        else{
            if (!(this.role instanceof Mayor) && this.isAlive){
                sendToCLient(colorStr("[GOD]:Waiting for the Mayor..." , "blue") , this);
                synchronized (this){
                    try {
                        this.wait();
                        return true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.role instanceof Mayor ){
                if (this.isAlive)
                    mayorAct(client);
                else {
                    client.isAlive=false;
                    sendToCLient(colorStr("[GOD]:Mayor voted to kill you. if you wanna see the rest of game type 1" , "cyan") , client);
                    String choice1 =client.in.readUTF();
                    client.canRecieve = choice1.equals("1");
                    sendToAll("GOD" , client.getName() + " died.");
                }
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

    /**
     * mayor action after voting
     * @param client player getting out by voting
     */
    public void mayorAct(Handler client){
        String choice;
        try {
            sendToCLient(colorStr("[GOD]:if you are agree to kill " + client.getName()+" type (yes) if not type (no)" , "cyan") , this);
            while (true) {
                choice = in.readUTF();
                if (choice.equals("yes")) {
                    client.isAlive = false;
                    client.canSpeak = false;
                    sendToCLient(colorStr("[GOD]:Mayor voted to kill you. if you wanna see the rest of the game type 1." , "cyan") , client);
                    String choice1 = client.in.readUTF();
                    client.canRecieve = choice1.equals("1");
                    sendToAll("GOD" , client.getName() + " died.");
                    break;
                } else if (choice.equals("no")) {
                    sendToCLient(colorStr("Mayor voted to save you." , "cyan") ,client);
                    sendToAll("GOD" ,"Mayor saved " + client.getName());
                    break;
                } else {
                    sendToCLient(colorStr("Wrong input" , "red") , this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * resetting voting results
     */
    public void resetVoting(){
        for (Handler client : clients){
            client.votes=0;
            client.votesTo = null;
        }
    }

    /**
     * find out who should leave the game by voting
     * @return leaving player
     */
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

    /**
     * finding a player by number of votes
     * @param max max vote
     * @return
     */
    public Handler findPlayerByVote(int max){
        for (Handler client : clients){
            if (client.getVotes()==max)
                return client;
        }
        return null;
    }

    /**
     * finding max number of votes
     * @return max votes
     */
    public int findMaxVote(){
        int max=0;
        for (Handler client : clients){
            if (client.getVotes()>max)
                max = client.getVotes();
        }
        return max;
    }

    /**
     * showing voting results
     * @throws IOException
     */
    public  void showVotes()throws IOException{
        int i=1;
        for (Handler c : clients){
            if (c.isAlive){
                sendToCLient(colorStr(i +")" + c.getName() +" has " + c.getVotes() +" votes." , "white") , this);
                i++;
                if (c.getVotes()>0) {
                    sendToCLient(colorStr("- These are the voters:" , "white") , this);
                    for (Handler voter : clients) {
                        if (voter.getVotesTo()!=null) {
                            if (voter.getVotesTo().getName().equals(c.getName())) {
                                sendToCLient(colorStr("   -" + voter.getName() , "white") , this);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * can vote a player or not
     * @param name a player vote to
     * @return if you can or not
     */
    public  boolean canVote(String name){
        if (name.equals("0"))
            return true;
        if (this.name.equals(name))
            return false;
        for (Handler client : clients){
            if (client.name.equals(name) && client.isAlive){
                return true;
            }
        }
        return false;
    }

    /**
     * submitting a vote
     * @param name a player who vote to
     */
    public void submitVote(String name){
        for (Handler client : clients){
            if (client.name.equals(name) && client.isAlive){
                this.votesTo = client;
                client.votes++;
            }
        }
    }

    //showing alive players list
    public void showPlayersList() throws IOException{
        int i = 1;
        for (Handler client : clients){
            if (client.isAlive) {
                sendToCLient(colorStr(i + ")" + client.getName()  , "purple") , this);
                i++;
            }
        }
    }

    /**
     * free chat mode actions
     * @throws IOException
     */
    public void freeChat()throws IOException {
        sendToCLient(colorStr("\n[GOD]:DAY CHAT TIMES START!\n" , "cyan") , this);
        long start = System.currentTimeMillis();
//        long end = start + 5 * 60 * 1000;
        long end = start + 30 * 1000;
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
                if (this.canSpeak && this.isAlive)
                    sendToAll(this.name, line);
                else {
                    sendToCLient(colorStr("[GOD]:You can't chat!" , "cyan") , this);
                }
            }
        }
        sendToCLient(colorStr("\n[GOD]:DAY CHAT TIMES UP\n" , "blue") , this);
    }

    /**
     * first night actions
     * @throws IOException
     */
    public void firstNight() throws IOException {
        //telling everybody for starting first night --------> do it better
        sendToCLient(colorStr("[GOD]:Hey everyone this is first night." , "cyan") , this);
        //telling mafia  ---------> do it better
        showMafias();
        //telling citizens -------> do it better
        if (this.getRole() instanceof Citizen){
            sendToCLient(colorStr("[GOD]:Your role is " + this.getRole().name  , "blue") , this);
            if (this.getRole() instanceof Mayor){
                Handler doc = findPlayerByRole("Doctor");
                sendToCLient(colorStr("[GOD]:" + doc.name + " is Doctor!" , "blue") , this);
            }
        }
    }

    /**
     * showing mafias to each other
     * @throws IOException
     */
    public void showMafias() throws IOException {
        int i = 1;
        if (this.role instanceof Mafia){
            if (server.getGame().getMafiaCount()==1) {
                sendToCLient(colorStr("You are " + this.getRole().name , "red") , this);
            } else {
                sendToCLient(colorStr("You are " + this.getRole().name + " and the other Mafias are:" , "red"), this);
                for (Handler client : clients) {
                    if (client.role.isMafia && !client.name.equals(this.name)) {
                        sendToCLient(colorStr(i + ")" + client.name + " is " + client.getRole().name , "red") , this);
                        i++;
                    }
                }
            }
        }
    }

    /**
     * finding a player by its role
     * @param role
     * @return
     */
    public Handler findPlayerByRole(String role){
        for (Handler client : clients){
            if (client.isAlive && client.getRole().name.equals(role)){
                return client;
            }
        }
        return null;
    }

    /**
     * for quiting from the chat
     * @throws IOException
     */
    public void quit() throws IOException{
        sendToCLient(colorStr("You left the game! if you want to see the rest of chat press 1 else 2!" , "cyan" ) , this);
        String command = in.readUTF();
        this.canRecieve = command.equals("1");
        this.canSpeak = false;
        this.isAlive = false;
        sendToAll("GOD" , this.name +" left the game!");
    }

    /**
     * for registering to the chat room
     * @throws IOException
     */
    public void register() throws IOException{
        while (true){
            sendToCLient(colorStr("Enter your username:" , "purple") , this);
            String name = in.readUTF();
            if (!names.contains(name) && !name.isBlank()){
                names.add(name);
                this.name = name;
                break;
            }else if (names.contains(name)){
                sendToCLient(colorStr("This user is in the game!Please choose another name." , "purple"), this );
            }
        }
        while (true){
            sendToCLient(colorStr("Type (ready) to join the game!" , "purple") , this);
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
                sendToCLient(colorStr("Wrong input." , "red") , this);
            }
        }
    }

    /**
     * sending a text to everyone
     * @param sender
     * @param msg
     */
    public void sendToAll(String sender , String msg){
        for (Handler client : clients){
            try {
                if (client.canRecieve) {
                    sendToCLient(colorStr("["+ sender +  "] : " , "yellow") + colorStr(msg , "green"), client);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * sending a massage for player
     * @param msg
     * @param client
     * @throws IOException
     */
    public void sendToCLient(String msg , Handler client) throws IOException {
        client.out.writeUTF(msg);
        client.out.flush();
    }

    public String colorStr(String msg , String color){
        switch (color){
            case "blue":
                return ANSI_BLUE + msg + ANSI_RESET;
            case "red":
                return ANSI_RED + msg + ANSI_RESET;
            case "yellow":
                return ANSI_YELLOW + msg + ANSI_RESET;
            case "purple":
                return ANSI_PURPLE + msg + ANSI_RESET;
            case "cyan":
                return ANSI_CYAN + msg + ANSI_RESET;
            case "green":
                return ANSI_GREEN + msg + ANSI_RESET;
            case "black":
                return ANSI_BLACK + msg + ANSI_RESET;
            case "white":
                return ANSI_WHITE + msg + ANSI_RESET;
        }
        return "";
    }
}