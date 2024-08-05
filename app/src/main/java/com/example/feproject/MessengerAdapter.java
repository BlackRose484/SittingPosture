

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

public class MessengerAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<MessengerModel> messages;
    int SENDER = 1;
    int RECEIVER = 0;

    public MessengerAdapter(Context context, ArrayList<MessengerModel> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new senderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false);
            return new receiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessengerModel message = messages.get(position);
        if (holder.getClass() == senderViewHolder.class) {
            senderViewHolder viewHolder = (senderViewHolder) holder;
            viewHolder.message.setText(message.getMessage());
            viewHolder.image.setImageResource(R.drawable.user2);
        } else {
            receiverViewHolder viewHolder = (receiverViewHolder) holder;
            viewHolder.message.setText(message.getMessage());
            viewHolder.image.setImageResource(R.drawable.computer);
        }
    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender() == SENDER) {
            return SENDER;
        } else {
            return RECEIVER;
        }
    }

    class senderViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView image;

        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.sender_text);
            image = itemView.findViewById(R.id.sender_img);
        }
    }

    class receiverViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView image;

        public receiverViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.receiver_text);
            image = itemView.findViewById(R.id.receiver_img);
        }
    }
}