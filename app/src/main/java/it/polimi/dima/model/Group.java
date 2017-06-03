package it.polimi.dima.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

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
import java.util.HashSet;
import java.util.Set;

import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;


/**
 * Created by Davide on 17/12/2016.
 */


public class Group {

    private Integer id;
    private String name;
    private String pictureURL;
    private Bitmap picture;
    private int isBusy;
    private int idBusy;
    private ArrayList<User> users = new ArrayList<User>();
    private Context c;
    private boolean isActive;

    // built group starting from id
    public Group(int id, Context c) throws JSONException {
        this.c = c;
        this.id = id;

        if(Utils.fileAlreadyExist(c, "SkiTalkGroupInfo"+id))
            loadGroup();
        else
            downloadGroup();
    }

    private void loadGroup(){
        BufferedReader input = null;
        File file = null;

        try {
            file = new File(c.getCacheDir(), "SkiTalkGroupInfo"+id); // Pass getFilesDir() and "MyFile" to read file

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
            setPicture();
            setMembers();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void downloadAndSaveGroup(int id, Context c) {
        //download and save group info
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroupInfo.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject group = request.getResponse();
        saveGroup(group, c);
        //download and save group members
        HttpRequest membersRequest = new HttpRequest("http://skitalk.altervista.org/php/getGroupMembers.php", "id=" + id);
        Thread t1 = new Thread(membersRequest);
        t1.start();
        JSONArray members = membersRequest.getArrayResponse();
        saveMembersList(members, id, c);
        //download and save group picture
        String pictureURL = null;
        try {
            pictureURL = group.getString("picture");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PictureDownloader pic = new PictureDownloader(pictureURL);
        Thread t2 = new Thread(pic);
        t2.start();
        Bitmap picture = pic.getPicture();
        Utils.putBitmapInDiskCache(c, pictureURL, picture);
    }

    private void downloadGroup() {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroupInfo.php", "id=" + id);
        Thread t = new Thread(request);
        t.start();
        JSONObject group = request.getResponse();
        saveGroup(group, c);
        try {
            name = group.getString("name");
            pictureURL = group.getString("picture");
            isBusy = group.getInt("isBusy");
            idBusy = group.getInt("idBusy");
            downloadPicture();
            setMembers();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveGroup(JSONObject group, Context c){
        String content = String.valueOf(group);

        System.out.println("Save values: "+content);
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(c.getCacheDir(), "SkiTalkGroupInfo"+group.getString("id"));

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPicture() {
        File cacheFile = new File(c.getCacheDir(), ""+pictureURL.hashCode());;
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

    // built group starting from id
    public void setGroup(JSONObject group) throws JSONException {
        this.id = group.getInt("id");
        name = group.getString("name");
        pictureURL = group.getString("picture");
        isBusy = group.getInt("isBusy");
        idBusy = group.getInt("idBusy");
        downloadPicture();
        setMembers();
    }

    //set all members of the group (only the id of the users)
    private void setMembers() throws JSONException {
        JSONArray members;

        //get the group's members
        if (!Utils.fileAlreadyExist(c, "SkiTalkGroupMembersInfo" + id))
            members = downloadMembersList();
        else
            members = loadMembersList();

        for (int i = 0; i < members.length(); i++) {
            int idUser = members.getJSONObject(i).getInt("id");
            this.users.add(new User(idUser, c));
        }
    }

    private JSONArray downloadMembersList() {
        HttpRequest membersRequest = new HttpRequest("http://skitalk.altervista.org/php/getGroupMembers.php", "id=" + id);
        Thread t1 = new Thread(membersRequest);
        t1.start();
        JSONArray members = membersRequest.getArrayResponse();
        saveMembersList(members, id, c);
        return members;
    }

    private JSONArray loadMembersList() {
        BufferedReader input = null;
        File file = null;
        JSONArray membersList;
        String cacheFileName = "SkiTalkGroupMembersInfo" + id;

        try {
            file = new File(c.getCacheDir(), cacheFileName);

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                membersList = new JSONArray(buffer.toString());
                System.out.println("Members list: " + membersList);
                return membersList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveMembersList(JSONArray members, int id, Context c) {
        String content = String.valueOf(members);

        File file;
        FileOutputStream outputStream;
        String cacheFileName = "SkiTalkGroupMembersInfo" + id;

        try {
            file = new File(c.getCacheDir(), cacheFileName);

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearGroupCache() {
        File file = new File(c.getCacheDir(), "SkiTalkGroupInfo"+id);
        file.delete();
        file = new File(c.getCacheDir(), "SkiTalkGroupMembersInfo"+id);
        file.delete();
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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        int savedActiveGroupID = sharedPref.getInt(c.getString(R.string.saved_active_group_id), -1);
        isActive = savedActiveGroupID == id;
        return isActive;
    }
}
