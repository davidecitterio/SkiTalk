package it.polimi.dima.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;



/**
 * Created by Davide on 29/12/2016.
 */

public class PictureDownloader implements Runnable{

    String src;
    Bitmap image;
    Boolean ready = false;

    public PictureDownloader (String src){
        this.src = src;
    }

    @Override
    public void run() {
        System.out.println("Download url "+src);
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

                image = myBitmap;
                ready = true;

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public Bitmap getPicture(){
        while(!ready);
        return image;
    }
}
