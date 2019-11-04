package com.example.scanimage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Main3Activity extends AppCompatActivity {
    TextView textData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        textData=findViewById(R.id.text_data);
        Intent intent=getIntent();
        String extra=intent.getStringExtra("Data");
        textData.setText(extra);
    }
}
