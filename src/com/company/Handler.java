package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Handler implements Runnable{

    private Socket socket;
    protected DataOutputStream out;
    private DataInputStream in;
    private String name;
    private ArrayList<Handler> clients;
    private static ArrayList<String> names = names = new ArrayList<>();

    public boolean canSpeak;
    public boolean canRecieve;
    public boolean isReady;

    public Handler(Socket socket ,ArrayList<Handler> clients){
        this.socket = socket;
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
                if (!isReady){
                    register();
                }
                line = in.readUTF();
                System.out.println(line);
                if (line.equals("quit")) {
                    quit();
                    return;
                }

                if (this.canSpeak)
                    sendToAll(this.name , line);
                else {
                    this.out.writeUTF("You cant chat!");
                    this.out.flush();
                }

            }
        }catch (IOException e){
            e.printStackTrace();
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
