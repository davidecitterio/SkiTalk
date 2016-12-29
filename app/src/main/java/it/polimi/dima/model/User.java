package it.polimi.dima.model;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



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

    //public constructor
    public User (int id) throws JSONException {
        this.id = id;
        HttpRequest request = new HttpRequest(address+"getUser.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();

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
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroups.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONArray groups = request.getArrayResponse();
        for (int i=0; i < groups.length(); i++) {
            this.groups.add(new Group(groups.getJSONObject(i).getInt("id")));
        }
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

}
