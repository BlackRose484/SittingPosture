package com.example.feproject;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIs {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static OkHttpClient client = new OkHttpClient();

    public static String checkPose(String base64_image) {
        // Create a JSON string
        String json = "{\"file\":\"" + base64_image + "\"}";

        // Create a JSON RequestBody
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url("http://192.168.0.105:8086/checkpose/")
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                return response.body().string();
            } else {
                return response.message();
            }
        } catch (IOException e) {
            Log.e("IOException %s", "Error", e);
        }
        return "";
    }

    public static void checkPoseAsync(String base64_image, Callback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String result = checkPose(base64_image);
            callback.onResult(result);
        });
        executorService.shutdown();
    }

    public interface Callback {
        void onResult(String result);
    }
}