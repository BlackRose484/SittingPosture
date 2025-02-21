package com.example.feproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {

    Button camera_btn, chat_btn, statistic_btn, blogs_btn;
    Animation rightleftAnim;
    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rightleftAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.rightleft_animation);

        camera_btn = findViewById(R.id.camera_btn);
        chat_btn = findViewById(R.id.chat_btn);
        statistic_btn = findViewById(R.id.statistic_btn);
        blogs_btn = findViewById(R.id.blogs_btn);

        camera_btn.setAnimation(rightleftAnim);
        chat_btn.setAnimation(rightleftAnim);
        statistic_btn.setAnimation(rightleftAnim);

        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Dashboard.this, Camera_Detect.class));
            }
        });

        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Dashboard.this, ChatWithAI.class));
            }
        });

        statistic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Dashboard.this, Statistic.class));
            }
        });

        blogs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Dashboard.this, Blogs.class));
            }
        });
    }
}