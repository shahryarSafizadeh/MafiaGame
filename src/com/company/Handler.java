package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

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
    public boolean hasRole = false;

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

//        if (game.isFirstNight()){
//            this.chatMode = "FIRSTNIGHT";
//        }else if (game.isDay() && !game.isVoting()){
//            this.chatMode = "FREECHAT";
//        }else if (game.isVoting()){
//            this.chatMode = "VOTING";
//        }else if (!game.isDay() && !game.isFirstNight()){
//            this.chatMode = "NIGHT";
//        }
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

                //running chat room
//                if (server.areAllPlayersReady()) {
//                    out.writeUTF("your role is " + this.role.name + " " + this.role.isMafia);
//                    out.flush();
//
//                    line = in.readUTF();
//                    System.out.println(line);
//                    if (line.equals("quit")) {
//                        quit();
//                        return;
//                    }
//
//                    if (this.canSpeak)
//                        sendToAll(this.name, line);
//                    else {
//                        this.out.writeUTF("You cant chat!");
//                        this.out.flush();
//                    }
//                }


                if (server.areAllPlayersReady()) {
                    this.mode = this.server.getGame().getMode();
                    switch (this.mode) {
                        case FIRSTNIGHT:
                            //first night things
                            firstNight();
                            break;
                        case FREECHAT:
                            //reechat things
                            break;
                        case VOTING:
                            //voting things
                            break;
                        case NIGHT:
                            //night things
                            break;
                    }
                    return;
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }
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
                this.out.close();
                this.in.close();
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
