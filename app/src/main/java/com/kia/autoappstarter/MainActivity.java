package com.kia.autoappstarter;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startAppStarterService();
    }

    private void startAppStarterService() {
        Intent intent = new Intent(this, AppStarterService.class);
        startService(intent);
    }
}
