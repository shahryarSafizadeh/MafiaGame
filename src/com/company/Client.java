package com.company;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 * class for client
 * @author shahryarsz
 * @version 1.1
 */
public class Client {
    /**
     * fields of client socket
     */
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader br;
    private Connection connection;

    /**
     * constructor for creating and doing client tasks
     * @param address server address
     * @param port server port
     */
    public Client(String address , int port ){
        try {
            socket = new Socket(address , port);
            System.out.println("Connected.");
            br = new BufferedReader(new InputStreamReader(System.in));
            out = new DataOutputStream(socket.getOutputStream());
            connection = new Connection(socket);
        }catch (IOException e){
            e.printStackTrace();
        }
        new Thread(connection).start();
        String line="";
        try {
            while (true){
                line = br.readLine();
                if (!line.isBlank()) {
                    out.writeUTF(line);
                    out.flush();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            out.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * main method for running a client
     * @param args
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter server port");
        int port = scanner.nextInt();
        Client client = new Client("127.0.0.1"  , port );
    }

}
