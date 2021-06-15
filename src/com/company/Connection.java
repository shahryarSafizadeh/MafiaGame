package com.company;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * connection class is for reading chats for client before client starts to chat
 * it implements Runnable
 * @author shahryarsz
 * @version 1.0
 */
public class Connection implements Runnable{
    /**
     * connection fields
     */
    private Socket socket;
    private DataInputStream in;

    /**
     * simple constructor for making connection for client socket
     * @param socket client socket
     */
    public Connection(Socket socket){
        try {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * overriding run method for connection
     */
    @Override
    public void run() {
        String line="";
        try {
            while (true){
                line = in.readUTF();
                System.out.println(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            in.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
