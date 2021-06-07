package com.company;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    private Socket socket;
    private DataOutputStream out;
    private BufferedReader br;
    private Connection connection;

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

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1"  , 8585 );
    }

}
