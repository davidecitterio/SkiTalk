package it.polimi.dima.model;

import android.content.Context;
import android.graphics.Bitmap;

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
    private Context c;


    // built group starting from id
    public Group(int id, Context c) throws JSONException {
        this.c = c;
        this.id = id;
        if (!alreadyExistGroup()) {
            HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroupInfo.php", "id=" + id);
            Thread t = new Thread(request);
            t.start();
            JSONObject group = request.getResponse();
            saveGroup(group);
            name = group.getString("name");
            pictureURL = group.getString("picture");
            isBusy = group.getInt("isBusy");
            idBusy = group.getInt("idBusy");
        }
        else
            loadGroup();
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

    public String getMembersString() {
        String members = new String();
        for (int j=0; j < users.size(); j++){
            members += users.get(j).getNickname();
            if (j+1 < users.size())
                members += ", ";
        }
        return members;
    }

    public boolean alreadyExistGroup(){
        File file = new File(c.getCacheDir(), "SkiTalkGroup"+id);
        if (file.exists()){
            return true;
        }
        else
            return false;
    }

    public void loadGroup(){
        BufferedReader input = null;
        File file = null;
        JSONArray jsonArr;
        JSONObject userInfo;

        try {
            file = new File(c.getCacheDir(), "SkiTalkGroup"+id); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            JSONObject group = new JSONObject(buffer.toString());
            name = group.getString("name");
            pictureURL = group.getString("picture");
            isBusy = group.getInt("isBusy");
            idBusy = group.getInt("idBusy");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void saveGroup(JSONObject group){
        String content = String.valueOf(group);

        System.out.println("Save values: "+content);
        File file;
        FileOutputStream outputStream;
        try {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(c.getCacheDir(), "SkiTalkGroup"+id);

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
