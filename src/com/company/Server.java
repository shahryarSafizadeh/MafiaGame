package com.company;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {


    private ServerSocket server;
    private Socket socket;
    private int playerCount;
    private ArrayList<Handler> clients = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(playerCount);
    private int readyPlayers = 0;
    private Game game = new Game(this);


    public Server(int port , int playerCount){
        try {
            server = new ServerSocket(port);
            System.out.println("server is waiting...");
            while (true){
                socket = server.accept();
                Handler handler = new Handler(socket , clients , this );
                clients.add(handler);
                pool.execute(handler);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Game getGame() {
        return game;
    }

    public ArrayList<Handler> getClients() {
        return clients;
    }

    public void increaseReadyPlayers() {
        readyPlayers++;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public synchronized boolean areAllPlayersReady() {
        return readyPlayers==playerCount;
    }



    public static void main(String[] args) {
        System.out.println("As server please enter how many players do you want in the game?(choose between 5-10)");
        Scanner scanner = new Scanner(System.in);
        int count = scanner.nextInt();

        Server server = new Server(8585 , count);


    }

}
