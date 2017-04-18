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
    private Integer speed;
    private ArrayList<Group> groups = new ArrayList<Group>();
    private Context c;

    //constructor for main user
    //onlyUser : if true it doesn't load the groups
    public User(int id, Context c, boolean onlyUser) {
        this.id = id;
        this.c = c;

        if (!Utils.fileAlreadyExist(c, "SkiTalkUserInfo"))
            downloadUser(onlyUser, true);
        else if(!isUserUpdated())
            downloadUser(onlyUser, true);
        else
            loadUser(onlyUser, true);
    }

    public static void downloadAndSaveUserInfo(int id, Context c) {
        //download and save user info
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getUser.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        //i add the id
        try {
            user.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveUserInfo(user, c, false);
        //download and save user picture
        String pictureURL = null;
        try {
            pictureURL = user.getString("picture");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PictureDownloader pic = new PictureDownloader(pictureURL);
        Thread t2 = new Thread(pic);
        t2.start();
        Bitmap picture = pic.getPicture();
        Utils.putBitmapInDiskCache(c, pictureURL, picture);
    }

    private void downloadUser(boolean onlyUser, boolean mainUser) {
        HttpRequest request = new HttpRequest(address + "getUser.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();

        //i add the id
        try {
            user.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        saveUserInfo(user, c, mainUser);

        try {
            name = user.getString("name");
            surname = user.getString("surname");
            email = user.getString("email");
            nickname = user.getString("nickname");
            pictureURL = user.getString("picture");
            ip = user.getString("ip");
            isOnline = user.getInt("isOnline");
            speed = user.getInt("speed");
            coords = new Coords(user.getDouble("latitude"), user.getDouble("longitude"));
            downloadPicture();
            if(!onlyUser)
                setGroups();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveUserInfo(JSONObject user, Context c, boolean mainUser) {
        String content = String.valueOf(user);

        if(mainUser)
            System.out.println("Save MAIN USER: " + content);
        else
            System.out.println("Save friend: " + content);

        File file;
        FileOutputStream outputStream;
        String cacheFileName;
        int id;
        try {
            id = user.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if(mainUser)
            cacheFileName = "SkiTalkUserInfo";
        else
            cacheFileName = "SkiTalkFriendInfo" + id;

        try {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(c.getCacheDir(), cacheFileName);

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveUserInfo(User user, Context c, boolean mainUser) {
        JSONObject u = new JSONObject();
        try {
            u.put("id", user.id);
            u.put("name", user.name);
            u.put("surname", user.surname);
            u.put("nickname", user.nickname);
            u.put("email", user.email);
            u.put("picture", user.pictureURL);
            u.put("isOnline", user.isOnline);
            u.put("ip", user.ip);
            u.put("speed", user.speed);
            u.put("latitude", user.coords.getLatitude());
            u.put("longitude", user.coords.getLongitude());
            saveUserInfo(u, c, mainUser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserUpdated() {/*
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
        }*/
        return true;
    }

    private void loadUser(boolean onlyUser, boolean mainUser) {
        BufferedReader input = null;
        File file = null;
        JSONObject userInfo;
        String cacheFileName;

        if (mainUser)
            cacheFileName = "SkiTalkUserInfo";
        else
            cacheFileName = "SkiTalkFriendInfo" + id;

        try {
            file = new File(c.getCacheDir(), cacheFileName); // Pass getFilesDir() and "MyFile" to read file

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
                speed = userInfo.getInt("speed");
                coords = new Coords(userInfo.getDouble("latitude"), userInfo.getDouble("longitude"));
                setPicture();
                if (!onlyUser)
                    setGroups();

                if (mainUser)
                    System.out.println("MAIN USER loaded.");
                else
                    System.out.println("Friend loaded");
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
        PictureDownloader pic = new PictureDownloader(pictureURL);
        Thread t = new Thread(pic);
        t.start();
        picture = pic.getPicture();
        Utils.putBitmapInDiskCache(c, pictureURL, picture);
    }

    private void downloadTempPicture() {
        PictureDownloader pic = new PictureDownloader(pictureURL);
        Thread t = new Thread(pic);
        t.start();
        picture = pic.getPicture();
    }

    // built user starting from json object, used for short-info user instance
    public User(JSONObject user) throws JSONException {
        id = user.getInt("id");
        name = user.getString("name");
        surname = user.getString("surname");
        nickname = user.getString("nickname");
        pictureURL = user.getString("picture");
        speed = user.getInt("speed");
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
        speed = user.getInt("speed");
    }

    public void updateGroups() {
        groups.clear();
        try {
            setGroups();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //set groups of the user
    private void setGroups() throws JSONException {
        JSONArray groups;

        if (!Utils.fileAlreadyExist(c, "SkiTalkGroupListInfo"))
            groups = downloadGroupList();
        else
            groups = loadGroupList(c);

        for(int i = 0; i < groups.length(); i++)
            this.groups.add(new Group(groups.getJSONObject(i).getInt("id"), c));
    }

    private JSONArray downloadGroupList() {
        HttpRequest groupsRequest = new HttpRequest(address + "getGroups.php", "id=" + id);
        Thread t1 = new Thread(groupsRequest);
        t1.start();
        JSONArray groups = groupsRequest.getArrayResponse();
        saveGroupsList(groups, c);
        return groups;
    }

    public static JSONArray loadGroupList(Context c) {
        BufferedReader input = null;
        File file = null;
        JSONArray groupList;
        String cacheFileName = "SkiTalkGroupListInfo";

        try {
            file = new File(c.getCacheDir(), cacheFileName);

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                groupList = new JSONArray(buffer.toString());
                System.out.println("Group list: " + groupList);
                return groupList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveGroupsList(JSONArray groups, Context c) {
        String content = String.valueOf(groups);

        File file;
        FileOutputStream outputStream;
        String cacheFileName = "SkiTalkGroupListInfo";

        try {
            file = new File(c.getCacheDir(), cacheFileName);

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //constructor of other users
    //n : 0 if user is updated, 1 if user need to be updated
    public User(int id, Context c) {
        this.id = id;
        this.c = c;

        if(!Utils.fileAlreadyExist(c, "SkiTalkFriendInfo" + id))
            downloadUser(true, false);
        else
            loadUser(true, false);
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

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
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

    public Integer getSpeed() {
        return speed;
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