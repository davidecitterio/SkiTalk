package it.polimi.dima.model;

import org.json.JSONException;
import org.json.JSONObject;

import static it.polimi.dima.model.HttpRequest.httpRequest;


/**
 * Created by Davide on 17/12/2016.
 */


//TODO develop code about picture (insert and upload)

public class User {

    private Integer id;
    private String name;
    private String surname;
    private String nickname;
    private String email;
    private String picture;
    private String ip;
    private Boolean isOnline;
    private Coords coords;


    public void User (int id) throws JSONException {
        this.id = id;
        setUser(id);
    }

    // built user starting from id
    public void setUser(int id) throws JSONException {


        JSONObject user = httpRequest("http://skitalk.altervista.org/getUser.php", "id="+id);

        name = user.getString("name");
        surname = user.getString("surname");
        email = user.getString("email");
        nickname = user.getString("nickname");
        picture = user.getString("picture");
        ip = user.getString("ip");
        isOnline = user.getBoolean("isOnline");
        coords.setCoords(user.getDouble("latitude"), user.getDouble("longitude"));

    }

    // built user starting from json object
    public void setUser(JSONObject user) throws JSONException {

        name = user.getString("name");
        surname = user.getString("surname");
        email = user.getString("email");
        nickname = user.getString("nickname");
        picture = user.getString("picture");
        ip = user.getString("ip");
        isOnline = user.getBoolean("isOnline");
        coords.setCoords(user.getDouble("latitude"), user.getDouble("longitude"));

    }

    //change nickname of user
    public void setNickname(String nickname) throws JSONException {
        JSONObject user = httpRequest("http://skitalk.altervista.org/editUserNickname.php", "id="+id+"&nickname="+nickname);
        setUser(user);
    }

    //change nickname of user
    public void setIp(String ip) throws JSONException {
        httpRequest("http://skitalk.altervista.org/setUserIp.php", "id="+id+"&ip="+ip);
        this.ip = ip;
    }

    //set user online
    public void setOnline() throws JSONException {
        JSONObject user =  httpRequest("http://skitalk.altervista.org/setUserOnline.php", "id="+id);
        setUser(user);
    }

    //set user offline
    public void setOffline() throws JSONException {
        JSONObject user =  httpRequest("http://skitalk.altervista.org/unsetUserOnline.php", "id="+id);
        setUser(user);
    }

    //set user coords
    public void setOnline(Coords coord) throws JSONException {
        JSONObject user =  httpRequest("http://skitalk.altervista.org/setUserOnline.php", "id="+id+"lat="+coord.getLatitude()+"long="+coord.getLongitude());
        setUser(user);
    }

    //set user km
    public void setKm(int km) throws JSONException {
        JSONObject user =  httpRequest("http://skitalk.altervista.org/setUserKm.php", "id="+id+"km="+km);
        setUser(user);
    }

    //set user isMoving
    public void setMoving() throws JSONException {
        JSONObject user =  httpRequest("http://skitalk.altervista.org/setUserMoving.php", "id="+id);
        setUser(user);
    }

    //set user not moving
    public void setNotMoving() throws JSONException {
        JSONObject user =  httpRequest("http://skitalk.altervista.org/unsetUserMoving.php", "id="+id);
        setUser(user);
    }

}
