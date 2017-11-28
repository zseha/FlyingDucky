package com.zs.flyingducky;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final Button buttonConnectToDevice = (Button)findViewById(R.id.buttonConnectToDevice);

        buttonConnectToDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               startActivity(new Intent(getApplicationContext(), DevicesActivity.class));
            }
        });
    }
}
