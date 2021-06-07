package com.company;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {


    private ServerSocket server;
    private Socket socket;
    private ArrayList<Handler> clients = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(6);
    private int readyPlayers = 0;


    public Server(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("server is waiting...");
            while (true){
                socket = server.accept();
                Handler handler = new Handler(socket , clients , this);
                clients.add(handler);
                pool.execute(handler);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        Server server = new Server(8585);
    }

    public void increaseReadyPlayers() {
        readyPlayers++;
    }

    public synchronized boolean areAllPlayersReady() {
        return readyPlayers==clients.size();
    }
}
