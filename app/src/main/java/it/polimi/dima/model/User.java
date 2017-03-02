package it.polimi.dima.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


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
    public User (int id, Context c)  {
        this.id = id;
        this.c = c;

        if (!alreadyExist("SkiTalkUserInfo")) {
            HttpRequest request = new HttpRequest(address + "getUser.php", "id=" + id);
            Thread t = new Thread(request);
            t.start();
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
                setPicture();
                setGroups();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
            loadUser(c);

    }

    public User (int id, int n)  {
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
                    setPicture();
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
        setPicture();
    }


    // built user starting from json object
    public void setUser(JSONObject user) throws JSONException {

        name = user.getString("name");
        surname = user.getString("surname");
        email = user.getString("email");
        nickname = user.getString("nickname");
        pictureURL = user.getString("picture");
        ip = user.getString("ip");
        isOnline =  user.getInt("isOnline");
        coords.setCoords(user.getDouble("latitude"), user.getDouble("longitude"));

    }

    //change nickname of user
    public void setNickname(String nickname) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/editUserNickname.php", "id="+id+"&nickname="+nickname);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //change nickname of user
    public void setIp(String ip) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserIp.php", "id="+id+"&ip="+ip);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        this.ip = ip;
    }

    //set user online
    public void setOnline() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserOnline.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user offline
    public void setOffline() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/unsetUserOnline.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user coords
    public void setOnline(Coords coord) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserOnline.php", "id="+id+"lat="+coord.getLatitude()+"long="+coord.getLongitude());
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user km
    public void setKm(int km) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserKm.php", "id="+id+"km="+km);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user isMoving
    public void setMoving() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setUserMoving.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user not moving
    public void setNotMoving() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/unsetUserMoving.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set groups of the user
    public void setGroups() throws JSONException {
        //if (!alreadyExist("SkiTalkUserGroupsInfo")){
            HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroups.php", "id="+id);
            Thread t = new Thread(request);
            t.start();
            JSONArray groups = request.getArrayResponse();
            saveGroups(groups);
            for (int i=0; i < groups.length(); i++) {
                this.groups.add(new Group(groups.getJSONObject(i).getInt("id"), c));
            }
        //}
        //else
        //    loadGroups();
    }

    public void setPicture(){
        PictureDownloader pic = new PictureDownloader(getPictureURL());
        Thread t = new Thread(pic);
        t.start();
        picture = pic.getPicture();
    }

    //getters
    public String getNickname(){
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
        if (isOnline==1) return true;
        else return false;
    }

    public Coords getCoords() {
        return coords;
    }

    public Integer getNumOfGroups(){
        return groups.size();
    }

    public Bitmap getPicture(){
        return picture;
    }

    public ArrayList<Group> getGroups(){
        return groups;
    }




    public void loadUser(Context c){
        BufferedReader input = null;
        File file = null;
        JSONArray jsonArr;
        JSONObject userInfo;

        try {
            file = new File(c.getCacheDir(), "SkiTalkUserInfo"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            System.out.println("Cache info: "+buffer.toString());

            try {

                userInfo = new JSONObject(buffer.toString());
                System.out.println("Cache info: "+userInfo);
                    nickname = userInfo.getString("nickname");
                    System.out.println("Nickname is: "+nickname);
                    name = userInfo.getString("name");
                    surname = userInfo.getString("surname");
                    email = userInfo.getString("email");
                    pictureURL = userInfo.getString("picture");
                    ip = userInfo.getString("ip");
                    isOnline = userInfo.getInt("isOnline");
                    coords = new Coords(userInfo.getDouble("latitude"), userInfo.getDouble("longitude"));
                    setPicture();
                    setGroups();
            } catch (JSONException e) {
                e.printStackTrace();
            }




            Log.d(TAG, buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveUserInfo(JSONObject user, Context c){
        String content = String.valueOf(user);

        System.out.println("Save values: "+content);
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

    public boolean alreadyExist(String name){
        File file = new File(c.getCacheDir(), name);
        if (file.exists())
            return true;
        else
            return false;
    }

    public void loadGroups(){
        BufferedReader input = null;
        File file = null;
        JSONArray jsonArr;
        JSONObject userInfo;

        try {
            file = new File(c.getCacheDir(), "SkiTalkUserGroupsInfo"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            JSONArray groups = new JSONArray(buffer.toString());
            for (int i=0; i < groups.length(); i++) {
                this.groups.add(new Group(groups.getJSONObject(i).getInt("id"), c));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void saveGroups(JSONArray groups){
        String content = String.valueOf(groups);

        System.out.println("Save values: "+content);
        File file;
        FileOutputStream outputStream;
        try {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(c.getCacheDir(), "SkiTalkUserGroupsInfo");

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


