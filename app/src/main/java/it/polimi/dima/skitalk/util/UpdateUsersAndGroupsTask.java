package it.polimi.dima.skitalk.util;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private final Object cacheLock;
    private boolean needUpdate;

    public UpdateUsersAndGroupsTask(Context c, User user, RecyclerGroupAdapter ca, Object cacheLock) {
        this.c = c;
        this.ca = ca;
        this.user = user;
        this.cacheLock = cacheLock;
        needUpdate = false;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        System.out.println("EXECUTING UPDATE TASK");
        System.out.println("Update task: waiting for lock...");
        synchronized (cacheLock) {
            System.out.println("Update task: lock obtained. Begin cache update");
            HttpRequest groupListRequest = runRequestGroupList(params[0]);
            HttpRequest groupNewsRequest = runRequestGroupNews(params[0]);
            HttpRequest friendsNewsRequest = runRequestNewsFromFriends(params[0]);

            //update and save groups list
            JSONArray oldGroupsList = User.loadGroupList(c);
            JSONArray newGroupsList = groupListRequest.getArrayResponse();
            boolean needListUpdate = !areEquals(oldGroupsList, newGroupsList);
            needUpdate = needListUpdate;
            if(needListUpdate)
                User.saveGroupsList(newGroupsList, c);

            //update and save groups
            JSONArray groupsNews = groupNewsRequest.getArrayResponse();
            try {
                if(groupsNews.getJSONObject(0).getInt("id") != -1) {
                    needUpdate = true;
                    for (int i = 0; i < groupsNews.length(); i++)
                        Group.downloadAndSaveGroup(groupsNews.getJSONObject(i).getInt("id"), c);
                }
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
            System.out.println("Update task: update finished. Notifying other threads");
        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if(needUpdate) {
            user.updateGroups();
            ca.notifyDataSetChanged();
        }
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

    private boolean areEquals(JSONArray a1, JSONArray a2) {
        boolean result = true;
        ArrayList<String> l1 = new ArrayList<String>(), l2 = new ArrayList<String>();
        try {
            for (int i = 0; i < a1.length(); i++)
                l1.add(a1.getString(i));
            for (int i = 0; i < a2.length(); i++)
                l2.add(a2.getString(i));
            if(!l1.equals(l2))
                result = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
