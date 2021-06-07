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
    private String role;
    private ArrayList<Handler> clients;
    private static ArrayList<String> names = names = new ArrayList<>();
    private Game game;

    public String getName() {
        return name;
    }

    public boolean canSpeak;
    public boolean canRecieve;
    public boolean isReady;

    private String chatMode;

    public Handler(Socket socket ,ArrayList<Handler> clients , Server server ){
        this.game = server.getGame();
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

                if (chatMode.equals("FIRSTNIGHT")){
                    //first night things!
                    firstNight();
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void firstNight() throws IOException {
        notifyAllClients("[GOD] : hey everyone this is first night and when i tell your roles the day beggin!");
        for (Handler client : clients){
            out.writeUTF("[GOD]:You are " + this.getRole(this));
        }
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
                this.socket.close();
                this.getRole(this);
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
                out.writeUTF("Tekrarie!!!");
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
                break;
            }else {
                out.writeUTF("Wrong input!");
                continue;
            }
        }
    }

    public String getRole(Handler client){
        return this.game.getPlayers().get(client).name;
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
