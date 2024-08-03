package com.example.feproject;

import org.chromium.net.CronetEngine;


import java.io.IOException;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;

public class APIs {
    public static final MediaType JSON = MediaType.get("application/json");

    public static OkHttpClient client = new OkHttpClient();

    public static String checkPose(String image) {
        RequestBody formBody = new FormBody.Builder()
                .add("file", image)
                .build();

        Request request = new Request.Builder()
                .url("http://127.0.0.1:8000/checkpose/")
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.message();
        } catch (IOException e) {
            System.out.printf("IOException %s", e.toString());
        }
        return "";
    }
}
