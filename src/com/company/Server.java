package com.company;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * server class for sending clients to client handler
 * @author shahryarsz
 * @version 1.0
 */
public class Server {
    /**
     * server fields
     */
    private ServerSocket server;
    private Socket socket;
    private int playerCount;
    private ArrayList<Handler> clients = new ArrayList<>();
    private ExecutorService pool ;
    private int readyPlayers = 0;
    private Game game;
    private ArrayList<Role> roles;

    /**
     * constructor for making connection between clients and server
     * @param port server port
     * @param playerCount game players count
     */
    public Server(int port , int playerCount){
        try {
            server = new ServerSocket(port);
            this.playerCount = playerCount;
            System.out.println("server is waiting...");
            pool = Executors.newFixedThreadPool(this.playerCount);
            game = new Game(this);
            this.roles = game.roleMaker(playerCount);
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

    /**
     * getting game
     * @return the game
     */
    public Game getGame() {
        return game;
    }

    /**
     * getting roles
     * @return roles
     */
    public ArrayList<Role> getRoles() {
        return roles;
    }

    /**
     * getting Handler list
     * @return clients
     */
    public ArrayList<Handler> getClients() {
        return clients;
    }

    /**
     * increasing ready players
     */
    public void increaseReadyPlayers() {
        readyPlayers++;
    }

    /**
     * getting players count
     * @return
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * check if everybody is ready to start the game
     * @return if ready : true else false
     */
    public synchronized boolean areAllPlayersReady() {
        return readyPlayers==playerCount;
    }

    /**
     * main method for running server
     * @param args
     */
    public static void main(String[] args) {
        int count;
        while (true) {
            System.out.println("As server please enter how many players do you want in the game?(choose between 5-10)");
            Scanner scanner = new Scanner(System.in);
            count = scanner.nextInt();
            if (count<5 || count>10)
                System.out.println("choose between 5 to 10!");
            else
                break;
        }
        Server server = new Server(6969 , count);
    }

}
