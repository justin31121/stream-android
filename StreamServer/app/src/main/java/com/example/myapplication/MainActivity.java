package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    private MediaController mediaController;
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int PORT = 6000;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;

        videoView = findViewById(R.id.vdVw);
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(context, "Failed to play Video", Toast.LENGTH_LONG).show();
            return true;
        });

        mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);

        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread(this));
        this.serverThread.start();

        startVideo("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private class ServerThread implements Runnable {
        private final Context context;

        public ServerThread(Context context){
            this.context = context;

            String ips="failure";

            WifiManager manager = (WifiManager) context
                    .getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            byte[] myIPAddress = BigInteger.valueOf(info.getIpAddress()).toByteArray();
            reverse(myIPAddress);
            try {
                InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);
                ips = myInetIP.getHostAddress();
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(context, "started Server\n"+ips, Toast.LENGTH_LONG).show();
        }

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (serverSocket!=null && !serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();

                    if(socket==null) continue;

                    CommunicationThread commThread = new CommunicationThread(context, socket);
                    new Thread(commThread).start();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try{
                if(socket!=null) socket.close();
                if(serverSocket!=null) serverSocket.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CommunicationThread implements Runnable {
        private Socket client;
        private BufferedReader input;
        private final Context context;

        public void run() {
            if(input==null) return;

            while (client!=null && !client.isClosed() && client.isConnected()) {
                if(client.isClosed()) {
                    break;
                }
                String read ="";
                try {
                    read = input.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(read==null) {
                    try {
                        client.close();
                        input.close();
                        client = null;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                try {
                    String finalRead = read;
                    runOnUiThread(() ->
                          Toast.makeText(context, finalRead, Toast.LENGTH_LONG).show()
                    );

                    Thread thread = new Thread(new StartVideoThread(finalRead));
                    thread.start();

                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            try{
                input.close();
                client.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        public CommunicationThread(Context context, Socket client) {
            this.client = client;
            this.context = context;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class StartVideoThread implements Runnable {

        private final String link;

        public StartVideoThread(String link) {
            this.link = link;
        }

        public void run() {
            try{
                runOnUiThread(() -> startVideo(link));
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void startVideo(String link) {
        //Location of Media File
        Uri uri1 = Uri.parse(link);
        //Starting VideView By Setting MediaController and URI
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(link);
        videoView.requestFocus();
        try {
            videoView.start();
        }
        catch(Exception e) {
            Toast.makeText(this, ""+e, Toast.LENGTH_LONG).show();
        }

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri1);
        videoView.requestFocus();

        try {
            videoView.start();
        }
        catch(Exception e) {
            Toast.makeText(this, ""+e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onStop();
        serverThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            onDestroy();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper())
                .postDelayed(() ->doubleBackToExitPressedOnce=false, 2000);
    }
}