package com.example.feproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.util.ArrayList;
import java.util.List;

public class Blogs extends AppCompatActivity {

    List<Article> articleList;
    BlogsAdapter adapter;
    RecyclerView blogsView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blogs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        blogsView = findViewById(R.id.blogs_recycle);
        progressBar = findViewById(R.id.progressbar);
        setupView();
        getNews();
    }

    private void setupView() {
        articleList = new ArrayList<>();
        blogsView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlogsAdapter(this, articleList);
        blogsView.setAdapter(adapter);
    }

    private void changeInProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(RecyclerView.VISIBLE);
        } else {
            progressBar.setVisibility(RecyclerView.INVISIBLE);
        }
    }

    private void getNews() {
        changeInProgress(true);
        NewsApiClient client = new NewsApiClient("65f45043e7844563b5f7d63101a4efbc");
        client.getTopHeadlines(
                new TopHeadlinesRequest.Builder().language("en").build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {

                        runOnUiThread(() -> {
                            changeInProgress(false);
                            articleList = response.getArticles();
                            adapter.updatedData(articleList);
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("News", throwable.getMessage());

                    }
                }
        );
    }
}