

package com.example.feproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feproject.MessengerModel;
import com.example.feproject.R;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class MessengerAdapter extends ArrayAdapter {
    Activity context;
    int id_layout;
    ArrayList<MessengerModel> messagesArrayList;


    public MessengerAdapter(Activity context, int id_layout, ArrayList<MessengerModel> messagesArrayList) {
        super(context, id_layout, messagesArrayList);
        this.context = context;
        this.id_layout = id_layout;
        this.messagesArrayList = messagesArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(id_layout, null);

        MessengerModel message = messagesArrayList.get(position);
        TextView messageView = convertView.findViewById(R.id.recivertextset);
        ImageView imageView = convertView.findViewById(R.id.pro);

        messageView.setText(message.getMessage());
        imageView.setImageResource(message.getSender());

        return convertView;
    }
}