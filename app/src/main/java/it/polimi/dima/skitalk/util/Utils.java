package it.polimi.dima.skitalk.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.adapter.RecyclerGroupAdapter;

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

    public static void pickImage(Activity ac) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ac.startActivityForResult(intent, 100);
    }

    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public static UpdateUsersAndGroupsTask updateUsersAndGroups(Context c, User user, RecyclerGroupAdapter ca, Object cacheLock) {
        UpdateUsersAndGroupsTask t = new UpdateUsersAndGroupsTask(c, user, ca, cacheLock);
        t.execute(user.getId());
        return t;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
