package com.example.meghana.post_data;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class Splash_activity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    if (!getSharedPreferences("login", Context.MODE_PRIVATE).getString("login", "false").equals("false")) {
                      //  intent.putExtra("uid", uid);


                        intent = new Intent(Splash_activity.this, Choose_students.class);
                    }
                    else {

                        intent = new Intent(Splash_activity.this, Login_activity.class);
                    }
                        startActivity(intent);
                        finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }
}
