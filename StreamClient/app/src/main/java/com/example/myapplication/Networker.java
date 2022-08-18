package com.example.myapplication;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Networker {

    private final static String ip = "192.168.178.49";
    private final static int port = 6000;

    private Socket socket = null;
    private PrintStream printStream = null;

    public Networker() {
    }

    public void post(String stream) {
        try {
            if (socket!=null && printStream!=null && stream != null) {
                printStream.println(stream);
                printStream.flush();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean connect() {
        try{
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 5000);
            printStream = new PrintStream(socket.getOutputStream());
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void disconnect() {
        if(socket!=null) {
            try {
                socket.close();
                socket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(printStream!=null) {
            try {
                printStream.close();
                printStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean connected() {
        if(socket==null) return false;
        return socket.isConnected();
    }
}
