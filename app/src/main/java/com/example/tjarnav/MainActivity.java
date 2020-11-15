package com.example.tjarnav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tjarnav.amp.AmapMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button ArBtn=(Button)findViewById(R.id.ArBtn);
        ArBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, ArActivity.class);
                startActivity(intent);
            }
        });
        Button NavBtn=(Button)findViewById(R.id.NavBtn);
        NavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, AmapMainActivity.class);
                startActivity(intent);
            }
        });
    }
}
