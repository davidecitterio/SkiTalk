package it.polimi.dima.model;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by Davide on 17/12/2016.
 */


//TODO implement picture managment
public class Group {

    private Integer id;
    private String name;
    private String picture;
    private int isBusy;
    private int idBusy;


    public void Group(int id, String name, String picture){

        this.id = id;
        this.name = name;
        this.picture = picture;
        isBusy = 0;
        idBusy = 0;
    }

    // built user starting from json object
    public void setGroup(JSONObject user) throws JSONException {

        name = user.getString("name");
        picture = user.getString("picture");
        isBusy = user.getInt("isBusy");
        idBusy = user.getInt("idBusy");

    }

    // return if the cgroup is busy
    public boolean isBusy(){
        if (isBusy==1) return true;
        else return false;
    }

    //try to put the group busy
    public void setBusy(int idUser) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/setGroupBusy.php", "idGroup="+id+"&idUser="+idUser);
        Thread t = new Thread(request);
        t.start();
        JSONObject response = request.getResponse();
        isBusy = response.getInt("isBusy");
    }

    // put the group free
    public void setNotBusy() throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/unsetGroupBusy.php", "idGroup=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject response = request.getResponse();
        isBusy = response.getInt("isBusy");
    }

    public void setName(String name) throws JSONException {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/editGroupName.php", "idGroup="+id+"&name="+name);
        Thread t = new Thread(request);
        t.start();
        JSONObject response = request.getResponse();
        setGroup(response);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public Boolean getIsBusy() {
        if (isBusy==1) return true;
        else return false;
    }

    public int getIdBusy() {
        return idBusy;
    }
}
