package com.largerlife.counterdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by László Gálosi on 28/03/16
 */
public class CountingImageDownloader extends AsyncTask<String, Integer, Bitmap> {
    private final TextView textView;
    private ImageView imageView;
    private int mCounter = 0;

    public CountingImageDownloader(TextView textView, ImageView imageView) {
        this.textView = textView;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap bitmap = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urls[0]).openConnection();
            connection.setRequestProperty("User-agent", "Mozilla/4.0");
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream in = null;
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
            }
            int length = connection.getContentLength();

            byte[] data = new byte[length];
            int increment = length / 10;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int count = -1;
            int progress = 0;
            while ((count = in.read(data, 0, increment)) != -1) {
                progress += count;
                mCounter = (progress * 10) / length;
                publishProgress(mCounter);
                outStream.write(data, 0, count);
            }
            bitmap = BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, data.length);
            in.close();
            outStream.close();
        } catch (Throwable throwable) {
            Log.e("Error", throwable.getMessage());
        }
        return bitmap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCounter = 0;
        textView.setText(String.format("%d", mCounter));
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        textView.setText(String.format("%d", values[0]));
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
