package com.example.tjarnav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tjarnav.ar.arcore.ArActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button ArBtn=(Button)findViewById(R.id.ArBtn);
//        ArBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(MainActivity.this, MapboxArActivity.class);
//                startActivity(intent);
//            }
//        });
//        Button NavBtn=(Button)findViewById(R.id.NavBtn);
//        NavBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(MainActivity.this, NavActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    public void startNav(View view) {
        Bundle bundle=new Bundle();
        bundle.putInt("type",1);
        Intent intent=new Intent(MainActivity.this, NavActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void startMapboxNav(View view) {
        Bundle bundle=new Bundle();
        bundle.putInt("type",2);
        Intent intent=new Intent(MainActivity.this, NavActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void startArTest(View view) {
        Intent intent=new Intent(MainActivity.this, ArActivity.class);
//        intent.putExtras(bundle);
        startActivity(intent);
    }
}
