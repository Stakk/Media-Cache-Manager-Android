package com.stakkfactory.cachedemo.network.api;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utils.FileUtils;

/**
 * Created by danielchung on 4/5/16.
 */
public class ApiGetVideo implements INetworkRequest<InputStream> {

    public final static String TAG = ApiGetVideo.class.getSimpleName();

    public interface OnGetVideoListener {
        void onGetVideo(String url, String videoPath);

        void onFailGetVideo(String url);
    }

    private String url;

    private OnGetVideoListener onGetVideoListener;

    public ApiGetVideo(String url) {
        this.url = url;
    }

    public ApiGetVideo setListener(OnGetVideoListener listener) {
        this.onGetVideoListener = listener;

        return this;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public boolean isDemoDataEnabled() {
        return false;
    }

    @Override
    public InputStream demoData() {
        return null;
    }

    @Override
    public void execute() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                final String urlString = ApiGetVideo.this.url;
                Log.d(TAG, "doInBackground: " + urlString);

                InputStream input = null;
                HttpURLConnection connection = null;

                String path = null;

                File temp;
                FileOutputStream output = null;

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

                    temp = File.createTempFile(FileUtils.md5(urlString), urlString.substring(urlString.lastIndexOf(".")));
                    temp.deleteOnExit();

                    path = temp.getAbsolutePath();

                    output = new FileOutputStream(temp);

                    FileUtils.copyStream(input, output);

                    Log.d(TAG, "Download completed to " + path);
                } catch (Exception e) {
                    e.printStackTrace();

                    return null;
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }

                        if (output != null) {
                            output.close();
                        }
                    } catch (IOException e) {

                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                return path;
            }

            @Override
            protected void onPostExecute(String path) {
                super.onPostExecute(path);
                if (path != null) {
                    if (onGetVideoListener != null) {
                        onGetVideoListener.onGetVideo(ApiGetVideo.this.url, path);
                    }
                } else {
                    if (onGetVideoListener != null) {
                        onGetVideoListener.onFailGetVideo(ApiGetVideo.this.url);
                    }
                }
            }
        }.execute();
    }
}
