package it.polimi.dima.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import it.polimi.dima.skitalk.util.Utils;

import static android.content.ContentValues.TAG;


/**
 * Created by Davide on 17/12/2016.
 */

public class User {

    private String address = "http://skitalk.altervista.org/php/";
    private Integer id;
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String pictureURL;
    private Bitmap picture;
    private String ip;
    private Integer isOnline;
    private Coords coords;
    private ArrayList<Group> groups = new ArrayList<Group>();
    private Context c;

    //public constructor
    public User(int id, Context c, boolean onlyUser) {
        this.id = id;
        this.c = c;

        if (!Utils.fileAlreadyExist(c, "SkiTalkUserInfo"))
            downloadUser(onlyUser);
        else if(!isUserUpdated())
            downloadUser(onlyUser);
        else
            loadUser(onlyUser);
    }

    private void downloadUser(boolean onlyUser) {
        HttpRequest request = new HttpRequest(address + "getUser.php", "id=" + id);
        Thread t2 = new Thread(request);
        t2.start();
        JSONObject user = request.getResponse();

        saveUserInfo(user, c);

        try {
            name = user.getString("name");
            surname = user.getString("surname");
            email = user.getString("email");
            nickname = user.getString("nickname");
            pictureURL = user.getString("picture");
            ip = user.getString("ip");
            isOnline = user.getInt("isOnline");
            coords = new Coords(user.getDouble("latitude"), user.getDouble("longitude"));
            downloadPicture();
            if(!onlyUser)
                setGroups();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveUserInfo(JSONObject user, Context c) {
        String content = String.valueOf(user);

        System.out.println("Save values: " + content);
        File file;
        FileOutputStream outputStream;
        try {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(c.getCacheDir(), "SkiTalkUserInfo");

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserUpdated() {
        HttpRequest newsRequest = new HttpRequest(address + "getNewsFromUser.php", "idUser=" + id);
        Thread t = new Thread(newsRequest);
        t.start();
        JSONObject news = newsRequest.getResponse();
        try {
            int id = news.getInt("id");
            return id == -1;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadUser(boolean onlyUser) {
        BufferedReader input = null;
        File file = null;
        JSONObject userInfo;

        try {
            file = new File(c.getCacheDir(), "SkiTalkUserInfo"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                userInfo = new JSONObject(buffer.toString());
                System.out.println("Cache info: " + userInfo);
                nickname = userInfo.getString("nickname");
                System.out.println("Nickname is: " + nickname);
                name = userInfo.getString("name");
                surname = userInfo.getString("surname");
                email = userInfo.getString("email");
                pictureURL = userInfo.getString("picture");
                ip = userInfo.getString("ip");
                isOnline = userInfo.getInt("isOnline");
                coords = new Coords(userInfo.getDouble("latitude"), userInfo.getDouble("longitude"));
                setPicture();
                if(!onlyUser)
                    setGroups();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d(TAG, buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setPicture() {
        File cacheFile = new File(c.getCacheDir(), ""+pictureURL.hashCode());
        // Open input stream to the cache file
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(cacheFile);
            picture = BitmapFactory.decodeStream(fis);
            System.out.println("Picture loaded from cache");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void downloadPicture() {
        PictureDownloader pic = new PictureDownloader(getPictureURL());
        Thread t = new Thread(pic);
        t.start();
        picture = pic.getPicture();
        Utils.putBitmapInDiskCache(c, pictureURL, picture);
    }

    private void downloadTempPicture() {
        PictureDownloader pic = new PictureDownloader(getPictureURL());
        Thread t = new Thread(pic);
        t.start();
        picture = pic.getPicture();
    }

    public User(int id, int n) {
        this.id = id;

        HttpRequest request = new HttpRequest(address + "getUser.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();

        try {
            nickname = user.getString("nickname");
            name = user.getString("name");
            surname = user.getString("surname");
            coords = new Coords(user.getDouble("latitude"), user.getDouble("longitude"));
            email = user.getString("email");
            pictureURL = user.getString("picture");
            ip = user.getString("ip");
            isOnline = user.getInt("isOnline");
            if (n != 0) {
                downloadPicture();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // built user starting from json object, used for short-info user instance
    public User(JSONObject user) throws JSONException {
        id = user.getInt("id");
        name = user.getString("name");
        surname = user.getString("surname");
        nickname = user.getString("nickname");
        pictureURL = user.getString("picture");
        downloadTempPicture();
    }


    // built user starting from json object
    public void setUser(JSONObject user) throws JSONException {
        name = user.getString("name");
        surname = user.getString("surname");
        email = user.getString("email");
        nickname = user.getString("nickname");
        pictureURL = user.getString("picture");
        ip = user.getString("ip");
        isOnline = user.getInt("isOnline");
        coords.setCoords(user.getDouble("latitude"), user.getDouble("longitude"));

    }

    //set groups of the user
    private void setGroups() throws JSONException {
        HttpRequest groupsRequest = new HttpRequest(address + "getGroups.php", "id=" + id);
        HttpRequest newsRequest = new HttpRequest(address + "getNewsFromGroup.php", "idUser=" + id);
        Thread t1 = new Thread(groupsRequest);
        Thread t2 = new Thread(newsRequest);
        t1.start();
        t2.start();
        JSONArray groups = groupsRequest.getArrayResponse();
        JSONArray news = newsRequest.getArrayResponse();

        for(int i = 0; i < groups.length(); i++)
            if(contains(news, groups.getJSONObject(i)))
                this.groups.add(new Group(groups.getJSONObject(i).getInt("id"), c, false));
            else
                this.groups.add(new Group(groups.getJSONObject(i).getInt("id"), c, true));
    }

    private boolean contains(JSONArray container, JSONObject item) {
        boolean isContained = false;
        try {
            for (int i = 0; !isContained && i < container.length(); i++)
                if (container.getJSONObject(i).equals(item))
                    isContained = true;
            return isContained;
        } catch(JSONException e) {
            e.printStackTrace();
            return true;
        }
    }

    //change nickname of user
    public void setNickname(String nickname) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/editUserNickname.php", "id=" + id + "&nickname=" + nickname);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //change nickname of user
    public void setIp(String ip) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserIp.php", "id=" + id + "&ip=" + ip);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        this.ip = ip;
    }

    //set user online
    public void setOnline() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserOnline.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user offline
    public void setOffline() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/unsetUserOnline.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user coords
    public void setOnline(Coords coord) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserOnline.php", "id=" + id + "lat=" + coord.getLatitude() + "long=" + coord.getLongitude());
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user km
    public void setKm(int km) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserKm.php", "id=" + id + "km=" + km);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user isMoving
    public void setMoving() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserMoving.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user not moving
    public void setNotMoving() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/unsetUserMoving.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }


    //getters
    public String getNickname() {
        return nickname;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public String getIp() {
        return ip;
    }

    public Boolean getIsOnline() {
        if (isOnline == 1) return true;
        else return false;
    }

    public Coords getCoords() {
        return coords;
    }

    public Integer getNumOfGroups() {
        return groups.size();
    }

    public Bitmap getPicture() {
        return picture;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

}