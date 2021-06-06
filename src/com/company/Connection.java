package com.company;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection implements Runnable{

    private Socket socket;
    private DataInputStream in;

    public Connection(Socket socket){
        try {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

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
