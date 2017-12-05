package com.zy.sharebook.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.zy.sharebook.R;
import com.zy.sharebook.util.PreferenceManager;

public class StartActivity extends AppCompatActivity {

    private final int DELAY_TIME = 4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toActivity();
            }
        }, DELAY_TIME);
    }

    public void toActivity() {
        finish();
        if(PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber").equals("")) {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
        } else {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
