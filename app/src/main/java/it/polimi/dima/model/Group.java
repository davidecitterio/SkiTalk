package it.polimi.dima.model;

import org.json.JSONException;
import org.json.JSONObject;

import static it.polimi.dima.model.HttpRequest.httpRequest;

/**
 * Created by Davide on 17/12/2016.
 */


//TODO implement picture managment
public class Group {

    private Integer id;
    private String name;
    private String picture;
    private boolean isBusy;
    private int idBusy;


    public void Group(int id, String name, String picture){

        this.id = id;
        this.name = name;
        this.picture = picture;
        isBusy = false;
        idBusy = 0;
    }

    // built user starting from json object
    public void setGroup(JSONObject user) throws JSONException {

        name = user.getString("name");
        picture = user.getString("picture");
        isBusy = user.getBoolean("isBusy");
        idBusy = user.getInt("idBusy");

    }

    // return if the cgroup is busy
    public boolean isBusy(){
        return isBusy;
    }

    //try to put the group busy
    public boolean setBusy(int idUser) throws JSONException {
        JSONObject response = httpRequest("http://skitalk.altervista.org/setGroupBusy.php", "idGroup="+id+"&idUser="+idUser);
        if (response.get("isBusy") == 1){
            isBusy = true;
            return true;
        }
        else
            return false;
    }

    // put the group free
    public boolean setNotBusy() throws JSONException {
        JSONObject response = httpRequest("http://skitalk.altervista.org/unsetGroupBusy.php", "idGroup="+id);
        if (response.get("isBusy") == 1){
            isBusy = false;
            return true;
        }
        else
            return false;

    }

    public void setName(String name) throws JSONException {
        JSONObject response = httpRequest("http://skitalk.altervista.org/editGroupName.php", "idGroup="+id+"&name="+name);
        setGroup(response);
    }
}
