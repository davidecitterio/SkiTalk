package it.polimi.dima.model;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Davide on 17/12/2016.
 */


//TODO develop code about picture (insert and upload)

public class User {

    private String address = "http://skitalk.altervista.org/php/";
    private Integer id;
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String picture;
    private String ip;
    private Integer isOnline;
    private Coords coords;
    private Group[] groups;


    public User (int id) throws JSONException {
        this.id = id;
        setUser(id);
    }

    // built user starting from id
    public void setUser(int id) throws JSONException {

        HttpRequest request = new HttpRequest(address+"getUser.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();

        name = user.getString("name");
        surname = user.getString("surname");
        email = user.getString("email");
        nickname = user.getString("nickname");
        picture = user.getString("picture");
        ip = user.getString("ip");
        isOnline = user.getInt("isOnline");
        coords = new Coords(user.getDouble("latitude"), user.getDouble("longitude"));
    }

    // built user starting from json object
    public void setUser(JSONObject user) throws JSONException {

        name = user.getString("name");
        surname = user.getString("surname");
        email = user.getString("email");
        nickname = user.getString("nickname");
        picture = user.getString("picture");
        ip = user.getString("ip");
        isOnline =  user.getInt("isOnline");
        coords.setCoords(user.getDouble("latitude"), user.getDouble("longitude"));

    }

    //change nickname of user
    public void setNickname(String nickname) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/editUserNickname.php", "id="+id+"&nickname="+nickname);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //change nickname of user
    public void setIp(String ip) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/setUserIp.php", "id="+id+"&ip="+ip);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        this.ip = ip;
    }

    //set user online
    public void setOnline() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/setUserOnline.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user offline
    public void setOffline() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/unsetUserOnline.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user coords
    public void setOnline(Coords coord) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/setUserOnline.php", "id="+id+"lat="+coord.getLatitude()+"long="+coord.getLongitude());
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user km
    public void setKm(int km) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/setUserKm.php", "id="+id+"km="+km);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user isMoving
    public void setMoving() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/setUserMoving.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //set user not moving
    public void setNotMoving() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/unsetUserMoving.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject user = request.getResponse();
        setUser(user);
    }

    //get nickname
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

    public String getPicture() {
        return picture;
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

}
