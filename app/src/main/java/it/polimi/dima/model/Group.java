package it.polimi.dima.model;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Davide on 17/12/2016.
 */


//TODO implement picture managment
public class Group {

    private Integer id;
    private String name;
    private String pictureURL;
    private Bitmap picture;
    private int isBusy;
    private int idBusy;
    private ArrayList<User> users = new ArrayList<User>();


    // built group starting from id
    public Group(int id) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroupInfo.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONObject group = request.getResponse();
        this.id = group.getInt("id");
        name = group.getString("name");
        pictureURL = group.getString("picture");
        isBusy = group.getInt("isBusy");
        idBusy = group.getInt("idBusy");
        setPicture();
        setMembers();
    }

    // built group starting from id
    public void setGroup(JSONObject group) throws JSONException {
        this.id = group.getInt("id");
        name = group.getString("name");
        pictureURL = group.getString("picture");
        isBusy = group.getInt("isBusy");
        idBusy = group.getInt("idBusy");
        setPicture();
        setMembers();
    }


    // return true if the group is busy
    public boolean isBusy(){
        if (isBusy==1) return true;
        else return false;
    }

    //try to put the group busy
    public void setBusy(int idUser) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/setGroupBusy.php", "idGroup="+id+"&idUser="+idUser);
        Thread t = new Thread(request);
        t.start();
        JSONObject response = request.getResponse();
        isBusy = response.getInt("isBusy");
    }

    // put the group free
    public void setNotBusy() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/unsetGroupBusy.php", "idGroup=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject response = request.getResponse();
        isBusy = response.getInt("isBusy");
    }

    //edit name of the group
    public void setName(String name) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/editGroupName.php", "idGroup="+id+"&name="+name);
        Thread t = new Thread(request);
        t.start();
        JSONObject response = request.getResponse();
        setGroup(response);
    }

    //set all members of the group (only the id of the users)
    public void setMembers() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroupMembers.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
        JSONArray users = request.getArrayResponse();
        for (int i=0; i< users.length(); i++){
           // inserisco tutti gli utenti, io incluso...
            this.users.add(new User(users.getJSONObject(i).getInt("id"), 0));
        }

    }

    public void setPicture(){
        PictureDownloader pic = new PictureDownloader(getPictureURL());
        Thread t = new Thread(pic);
        t.start();
        picture = pic.getPicture();
    }

    //getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public Boolean getIsBusy() {
        if (isBusy==1) return true;
        else return false;
    }

    public int getIdBusy() {
        return idBusy;
    }

    public Bitmap getPicture(){
        return picture;
    }

    public ArrayList<User> getMembers(){
        return users;
    }
}
