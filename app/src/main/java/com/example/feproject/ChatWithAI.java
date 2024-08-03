package com.example.feproject;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

public class ChatWithAI extends AppCompatActivity {

    int imageUser[] = {R.drawable.computer, R.drawable.user};
    CardView send_btn;
    LinearLayout edit_text;
    EditText text_msg;
    int sender_id = 1;
    SQLiteDatabase sqlite;
    MessengerAdapter adapter;
    ListView messageView;
    ArrayList<MessengerModel> list_messages = new ArrayList<>();
    int screenWidth = 0, screenHeight = 0;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_with_ai);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        send_btn = findViewById(R.id.sendbtn);
        text_msg = findViewById(R.id.textmsg);
        edit_text = findViewById(R.id.listview_text);
        messageView = findViewById(R.id.msg_adapter);
        adapter = new MessengerAdapter(this, R.layout.receiver_layout, list_messages);
        messageView.setAdapter(adapter);

        sqlite = openOrCreateDatabase("chatbot", MODE_PRIVATE, null);
        try {
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS chatbot(sender INT, message VARCHAR, time VARCHAR)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        getMessages();
        adapter.notifyDataSetChanged();

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                String message = text_msg.getText().toString();
                adapter.notifyDataSetChanged();
                text_msg.setText("");
                sqlite.execSQL("INSERT INTO chatbot(sender, message, time) VALUES('" + sender_id + "', '" + message + "', '" + date.toString() + "')");

                getMessages();
                adapter.notifyDataSetChanged();
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        text_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_text.setY((float) (screenHeight * 0.68));
            }
        });

        text_msg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    edit_text.setY((float) (screenHeight * 0.68));
                } else {
                    edit_text.setY((float) (screenHeight * 0.98));
                }
            }
        });
        messageView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                edit_text.setY((float) (screenHeight * 0.95));
            }
        });
    }

    private void getMessages() {
        list_messages.clear();
        Cursor c = sqlite.query("chatbot", null, null, null, null, null, null);
        c.moveToNext();
        while (c.isAfterLast() == false) {
            String message = c.getString(1);
            int sender = c.getInt(0);
            String time = c.getString(2);
            if (sender == 1) {
                list_messages.add(new MessengerModel(message, R.drawable.user, time));
            } else {
                list_messages.add(new MessengerModel(message, R.drawable.computer, time));
            }
            c.moveToNext();
        }
        c.close();

        while (list_messages.size() > 10) {
            list_messages.remove(0);
        }
    }

}