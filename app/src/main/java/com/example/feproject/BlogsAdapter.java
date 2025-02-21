package com.example.feproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kwabenaberko.newsapilib.models.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BlogsAdapter extends RecyclerView.Adapter<BlogsAdapter.BlogsViewHolder> {

    Context context;
    List<Article> articleList;
    public BlogsAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public BlogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blogs_item, parent, false);
        return new BlogsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogsViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.title.setText(article.getTitle());
        holder.src.setText(article.getSource().getName());

        Picasso.get().load(article.getTitle())
                .error(R.drawable.no_image)
                .into(holder.image);
        holder.itemView.setOnClickListener((v) -> {
           Intent intent = new Intent(v.getContext(), BlogFullView.class);
           intent.putExtra("url", article.getUrl());
           v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (articleList != null) {
            return articleList.size();
        }
        return 0;
    }

    void updatedData(List<Article> data) {
        articleList.clear();
        articleList.addAll(data);
    }

    class BlogsViewHolder extends RecyclerView.ViewHolder {
        TextView title, src;
        ImageView image;

        public BlogsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.blog_title);
            src = itemView.findViewById(R.id.blog_src);
            image = itemView.findViewById(R.id.blog_image);
        }
    }
}
