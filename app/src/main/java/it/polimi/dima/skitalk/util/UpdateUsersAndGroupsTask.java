package it.polimi.dima.skitalk.util;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import it.polimi.dima.model.Group;
import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.adapter.RecyclerGroupAdapter;

/**
 * Created by Max on 14/03/2017.
 */

public class UpdateUsersAndGroupsTask extends AsyncTask<Integer, Void, Boolean> {
    private Context c;
    private User user;
    private RecyclerGroupAdapter ca;

    public UpdateUsersAndGroupsTask(Context c, User user, RecyclerGroupAdapter ca) {
        this.c = c;
        this.ca = ca;
        this.user = user;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        HttpRequest groupListRequest = runRequestGroupList(params[0]);
        HttpRequest groupNewsRequest = runRequestGroupNews(params[0]);
        HttpRequest friendsNewsRequest = runRequestNewsFromFriends(params[0]);

        //update and save groups list
        JSONArray groupsList = groupListRequest.getArrayResponse();
        User.saveGroupsList(groupsList, c);

        //update and save groups
        JSONArray groupsNews = groupNewsRequest.getArrayResponse();
        try {
            for(int i = 0; i < groupsNews.length(); i++)
                Group.downloadAndSaveGroup(groupsNews.getJSONObject(i).getInt("id"), c);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //update and save friends
        Set<Integer> result = new HashSet<>();
        JSONArray users = friendsNewsRequest.getArrayResponse();
        for (int i=0;  i < users.length(); i++){
            try {
                result.add(users.getJSONObject(i).getInt("idFriend"));
            } catch (JSONException e) {
                //if the exception is caught, it means that the response is [{"id":"-1"}]
                //so return the empty set
            }
        }
        for(int id : result)
            User.downloadAndSaveUserInfo(id, c);

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        user.updateGroups();
        ca.notifyDataSetChanged();
    }

    private HttpRequest runRequestGroupList(int id) {
        HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/getGroups.php", "id=" + id);
        Thread t1 = new Thread(groupsRequest);
        t1.start();
        return groupsRequest;
    }

    private HttpRequest runRequestGroupNews(int id) {
        HttpRequest newsRequest = new HttpRequest("http://skitalk.altervista.org/php/getNewsFromGroup.php", "idUser=" + id);
        Thread t2 = new Thread(newsRequest);
        t2.start();
        return newsRequest;
    }

    private HttpRequest runRequestNewsFromFriends(int id) {
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getNewsFromFriends.php", "idUser=" + id);
        Thread t = new Thread(request);
        t.start();
        return request;
    }

    private boolean contains(JSONArray container, JSONObject item) {
        boolean isContained = false;
        try {
            for (int i = 0; !isContained && i < container.length(); i++)
                if (container.getJSONObject(i).equals(item))
                    isContained = true;
            return isContained;
        } catch(JSONException e) {
            e.printStackTrace();
            return true;
        }
    }
}
