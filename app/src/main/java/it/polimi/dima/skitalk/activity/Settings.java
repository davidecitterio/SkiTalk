package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.adapter.RecyclerAddUserAdapter;
import it.polimi.dima.skitalk.adapter.RecyclerAddedUserAdapter;
import it.polimi.dima.skitalk.util.ActivityWithRecyclerView;
import it.polimi.dima.skitalk.util.DividerItemDecoration;
import it.polimi.dima.skitalk.util.Utils;
import it.polimi.dima.skitalk.util.VerticalSpacingDecoration;

/**
 * Created by Davide on 30/12/2016.
 */

public class Settings extends Activity implements ActivityWithRecyclerView {
    private static RecyclerAddUserAdapter tempUsersAdapter;
    private static RecyclerAddedUserAdapter membersAdapter;
    private Button create;
    private EditText search_user;
    private String name;
    private Bitmap picture;
    private static ArrayList<User> tempUsers = new ArrayList<>();
    private static ArrayList<User> members = new ArrayList<>();
    private static ArrayList<Integer> users = new ArrayList<>();
    private int id, idGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitleTextColor(Color.WHITE);
        tb.setTitle(getString(R.string.settings));

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        name = intent.getStringExtra("name");
        System.out.println(id);
        System.out.println(name);
        picture = (Bitmap) intent.getParcelableExtra("picture");

        search_user = (EditText) findViewById(R.id.search_user);

        CircleImageView imageView = (CircleImageView) findViewById(R.id.group_toolbar_picture);
        imageView.setImageBitmap(picture);

        TextView nameGroup = (TextView) findViewById(R.id.nameGroup);
        nameGroup.setText(name);

        search_user.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //hide keyboard
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(search_user.getWindowToken(), 0);

                    performSearch();

                    return true;
                }
                return false;
            }
        });

        create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (users.size() > 0){

                    createGroup();

                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
                    alert.setTitle(R.string.create_group_problem_title);
                    alert.setMessage(R.string.create_group_problem_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            }
        });

        RecyclerView membersRecyclerView = (RecyclerView) findViewById(R.id.members_recycler_view);
        membersAdapter = new RecyclerAddedUserAdapter(members, this);
        setupRecyclerView(membersRecyclerView, membersAdapter);
        RecyclerView tempUsersRecyclerView = (RecyclerView) findViewById(R.id.temp_users_recycler_view);
        tempUsersAdapter = new RecyclerAddUserAdapter(tempUsers, this);
        setupRecyclerView(tempUsersRecyclerView, tempUsersAdapter);
    }

    public void createGroup() {
        new CreateGroup().execute("");
    }

    private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        int spacing = getResources().getInteger(R.integer.group_creation_recycler_spacing);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new VerticalSpacingDecoration(spacing));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.item_decorator), spacing, false));
        //layout
        LinearLayoutManager llm = new LinearLayoutManager(Settings.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
    }

    private void performSearch() {
        if (search_user.getText().toString().length() > 0) {
            tempUsers.clear();
            searchUser(search_user.getText().toString());
            showSearchResult();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
            alert.setTitle("Oooops.");
            alert.setMessage("You have to fill the nickname field");
            alert.setPositiveButton("OK", null);
            alert.show();
        }
    }

    public void searchUser(String user){
        final ProgressDialog progressDialog = new ProgressDialog(Settings.this,
                ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();

        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/searchUser.php", "nickname="+user);
        Thread t = new Thread(request);
        t.start();
        JSONArray response = request.getArrayResponse();

        String members = new String();

        for (int i=0; i<response.length(); i++){

            try {
                if (response.getJSONObject(i).getInt("id") == -1) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
                    alert.setTitle(R.string.no_users_found_title);
                    alert.setMessage(R.string.no_users_found_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
                else {
                    if (!alreadyPresent(response.getJSONObject(i).getInt("id")) && response.getJSONObject(i).getInt("id") != id)
                        tempUsers.add(new User(response.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        progressDialog.dismiss();
    }

    public boolean alreadyPresent(int id){
        for (int i = 0; i< users.size(); i++){
            if (users.get(i) == id)
                return true;
        }
        return false;
    }

    private void showSearchResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempUsersAdapter.notifyDataSetChanged();

                if (tempUsers.size()==0){
                    AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
                    alert.setTitle(R.string.no_users_found_title);
                    alert.setMessage(R.string.no_users_found_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                    return;
                }

                System.out.println(tempUsers.size());
                //modify this for item spacing
            }
        });
    }

    @Override
    public void addUser(int id){
        for (User user : tempUsers) {
            if (user.getId() == id){
                users.add(user.getId());
                members.add(user);
                tempUsers.remove(user);
                membersAdapter.notifyDataSetChanged();
                tempUsersAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void removeUser(int id){
        for (User user : members) {
            if (user.getId() == id){
                users.remove(user.getId());
                members.remove(user);
                membersAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    private void uploadImage(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://skitalk.altervista.org/php/editGroupPicture.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        //Showing toast
                        Toast.makeText(Settings.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = Utils.getStringImage(picture);

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("picture", image);
                params.put("name", "pic_"+idGroup);
                params.put("id", String.valueOf(idGroup));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }



    private class CreateGroup extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(Settings.this,
                ProgressDialog.STYLE_SPINNER);


        @Override
        protected Boolean doInBackground(String... params) {

            try {
                HttpRequest request= new HttpRequest("http://skitalk.altervista.org/php/addGroup.php",
                        "name="+ URLEncoder.encode(name, "UTF-8")+"&id="+id);
                Thread tr = new Thread(request);
                tr.start();
                JSONObject response = request.getResponse();

                idGroup = response.getInt("id");

                System.out.println("id of the new group is: "+idGroup);

                uploadImage();

                return true;

            }   catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                progressDialog.dismiss();
                new AddMembers().execute("");
            }
            else
                System.out.println("Nooooooooo");
        }

        @Override
        protected void onPreExecute() {

            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.creating_group));
            progressDialog.show();
        }
    }


    private class AddMembers extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog2 = new ProgressDialog(Settings.this,
                ProgressDialog.STYLE_SPINNER);

        @Override
        protected Boolean doInBackground(final String... arg) {

            for (int i=0; i<users.size();i++){
                System.out.println("Req n. "+i);
                HttpRequest request= new HttpRequest("http://skitalk.altervista.org/php/addUserToGroup.php",
                        "idGroup="+idGroup+"&idUser="+users.get(i));
                Thread tr = new Thread(request);
                tr.start();


            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success)
                progressDialog2.dismiss();
            Intent myIntent = new Intent(Settings.this, GroupActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("userId",id);
            extras.putInt("groupId",idGroup);
            myIntent.putExtras(extras);
            Settings.this.startActivity(myIntent);

            finish();
        }

        @Override
        protected void onPreExecute() {
            System.out.println(idGroup);
            System.out.println("size= "+users.size());

            progressDialog2.setIndeterminate(true);
            progressDialog2.setMessage(getString(R.string.adding_members));
            progressDialog2.show();
        }
    }
}
