package com.stakkfactory.cachedemo.network.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiGetImage implements INetworkRequest<Bitmap> {

    public final static String TAG = ApiGetImage.class.getSimpleName();

    public interface OnGetImageListener {
        void onGetImage(String url, Bitmap image);

        void onFailGetImage(String url);
    }

    private String url;

    private OnGetImageListener onGetImageListener;

    public ApiGetImage(String url) {
        this.url = url;
    }

    public ApiGetImage setListener(OnGetImageListener listener) {
        this.onGetImageListener = listener;

        return this;
    }

    @Override
    public String url() {
        return this.url;
    }

    @Override
    public boolean isDemoDataEnabled() {
        return false;
    }

    @Override
    public Bitmap demoData() {
        return null;
    }

    @Override
    public void execute() {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                final String urlString = ApiGetImage.this.url;
                Log.d(TAG, "doInBackground: " + urlString);

                InputStream input = null;
                HttpURLConnection connection = null;

                Bitmap bitmap = null;

                try {
                    URL url = new URL(urlString);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage());
                    }

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();

                    bitmap = BitmapFactory.decodeStream(input);
                } catch (Exception e) {
                    e.printStackTrace();

                    return null;
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException e) {

                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    if (onGetImageListener != null) {
                        onGetImageListener.onGetImage(ApiGetImage.this.url, bitmap);
                    }
                } else {
                    if (onGetImageListener != null) {
                        onGetImageListener.onFailGetImage(ApiGetImage.this.url);
                    }
                }
            }
        }.execute();
    }
}
