package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    private VideoView videoView;
    private MediaController mediaController;
    private String link = "empty";
    private Context context;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);

        context = this;

        videoView = findViewById(R.id.vdVw);
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(context, "Failed to play Video", Toast.LENGTH_LONG).show();
            return true;
        });

        mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);

        retrieveMessage();

        Toast.makeText(this, link, Toast.LENGTH_LONG).show();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startVideo(link);
    }

    private void retrieveMessage() {
        link = getIntent().getStringExtra("message");
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
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to leave Player", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper())
                .postDelayed(() -> doubleBackToExitPressedOnce=false,2000);
    }
}
