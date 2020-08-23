package com.example.alistair.finaltakemehome;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Splash_Activity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActionBar actionBar = getSupportActionBar(); if(actionBar != null)

        {
            actionBar.hide(); } new Handler().postDelayed(new Runnable()
        {

            @Override public void run(){ Intent startActivityIntent = new Intent(Splash_Activity.this, MainActivity.class);
                startActivity(startActivityIntent);
                Splash_Activity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}