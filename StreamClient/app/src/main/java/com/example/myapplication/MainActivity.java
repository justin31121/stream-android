package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Networker networker;

    private TextView log = null;
    private Scraper scraper;

    private Button connect_button;
    private EditText editText;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.log = findViewById(R.id.status);
        editText = findViewById(R.id.link);
        connect_button = findViewById(R.id.connect_button);
        Button launch_button = findViewById(R.id.launch_button);

        networker = new Networker();

        scraper = new Scraper(this);

        connect_button.setOnClickListener((view) -> {
            boolean connected = networker.connected();

            networker.disconnect();
            disconnect();
            if (!connected) {
                searchForServer();
            }
        });

        launch_button.setOnClickListener((view) -> launchInput());

        editText.setOnKeyListener((view, keyCode, event) -> {
            boolean enterPressed = (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER);
            if (!enterPressed) return false;
            launchInput();
            return true;
        });

        searchForServer();
    }

    private void launchInput() {
        String url = enter(editText);
        if(url.length()<2) return;
        if (networker.connected()) {
            (new Thread(new RequestThread(getApplicationContext(), url))).start();
        } else {
            (new Thread(new StartStreamThread(getApplicationContext(), url))).start();
        }
    }

    private void connect(boolean success) {
        if (success) {
            log.setText(R.string.connected);
            connect_button.setText(R.string.disconnect);
        } else {
            log.setText(R.string.connect_fail);
        }
    }

    private void disconnect() {
        log.setText(R.string.disconnected);
        connect_button.setText(R.string.connect);
    }

    private String enter(EditText editText) {
        String url = editText.getText().toString();
        editText.setText("");
        return url;
    }

    private void switchActivity(String message) {
        Intent switchActivity = new Intent(this, VideoActivity.class);
        switchActivity.putExtra("message", message);
        startActivity(switchActivity);
    }

    private class PostStreamThread implements Runnable {

        private final String stream;

        public PostStreamThread(String stream) {
            this.stream = stream;
        }

        public void run() {
            networker.post(stream);
        }
    }

    private class RequestThread implements Runnable {

        private final Context context;
        private final String url;

        public RequestThread(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        void toast(String message) {
            try {
                runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if ((!networker.connected()) || url == null) return;

            int len = url.length();
            String extension = url.substring(len - 4, len);
            if ("m3u8".equals(extension)) {
                (new Thread(new MainActivity.PostStreamThread(url))).start();
                return;
            }

            scraper.requestUrl(url,
                    (response) -> {
                        String stream = scraper.extractUrls(response);
                        (new Thread(new MainActivity.PostStreamThread(stream))).start();
                    },
                    (error) -> toast("Can not resolve url " + url)
            );
        }
    }

    private class StartStreamThread implements Runnable {

        private final Context context;
        private final String url;

        public StartStreamThread(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        void toast(String message) {
            try {
                runOnUiThread(() -> Toast.makeText(context, "" + message, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            scraper.requestUrl(url, (response) -> {
                String stream = scraper.extractUrls(response);

                toast(stream);
                if (stream != null) {
                    runOnUiThread(() -> switchActivity(stream));
                }
            }, (error) -> toast("Can not resolve url " + url));
        }
    }

    private void searchForServer() {
        log.setText(R.string.connecting);

        Thread thread = new Thread(new ClientThread());
        thread.start();
    }

    private class ClientThread implements Runnable {

        public void run() {
            boolean success = networker.connect();
            runOnUiThread(() -> connect(success));
            if (!success) return;

            //Wait 60 seconds
            try {
                Thread.sleep(1000 * 60);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Close Socket
            networker.disconnect();
            runOnUiThread(MainActivity.this::disconnect);
        }
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () ->  doubleBackToExitPressedOnce = false,
                        2000);
    }
}
