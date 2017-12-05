package com.zy.sharebook.network;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by ZY on 2017/11/13.
 */

public class HttpHelper {
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    /**发送请求**/
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendOKHttpPost(String json, String url, okhttp3.Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static void uploadImage(String url, File file, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody=  new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file))
                .build();

        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}
