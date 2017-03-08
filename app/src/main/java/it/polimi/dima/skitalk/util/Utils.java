package it.polimi.dima.skitalk.util;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Max on 03/03/2017.
 */

public class Utils {
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static void putBitmapInDiskCache(Context c, String url, Bitmap picture) {
        // Create a path pointing to the system-recommended cache dir for the app, with sub-dir named
        // thumbnails
        File cacheFile = new File(c.getCacheDir(), ""+url.hashCode());
        try {
            // Create a file at the file path, and open it for writing obtaining the output stream
            cacheFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(cacheFile);
            // Write the bitmap to the output stream (and thus the file) in PNG format (lossless compression)
            picture.compress(Bitmap.CompressFormat.PNG, 100, fos);
            // Flush and close the output stream
            fos.flush();
            fos.close();
        } catch (Exception e) {
            System.err.println("Error when saving image to cache.\n " + e);
        }
    }

    public static boolean fileAlreadyExist(Context c, String name) {
        File file = new File(c.getCacheDir(), name);
        return file.exists();
    }
}
