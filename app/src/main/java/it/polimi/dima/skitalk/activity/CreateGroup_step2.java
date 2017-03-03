package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.io.ByteArrayOutputStream;
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
import it.polimi.dima.skitalk.util.DividerItemDecoration;
import it.polimi.dima.skitalk.util.VerticalSpacingDecoration;

/**
 * Created by Davide on 30/12/2016.
 */

public class CreateGroup_step2 extends Activity{
    Button create, search, addUser;
    EditText search_user;
    static TextView members;
    String name;
    Bitmap picture;
    static ArrayList<User>tempUsers = new ArrayList<>();
    static ArrayList<Integer>users = new ArrayList<>();
    int id, idGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_step_2);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitleTextColor(Color.WHITE);
        tb.setTitle(getString(R.string.new_group));

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

        members = (TextView) findViewById(R.id.members);
        members.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (members.getText().toString().length()>0){
                    System.out.println("s è: "+members.getText());
                    String[] separated = members.getText().toString().split(";");
                    System.out.println("separated lenght: "+separated.length);

                    addTempUser(Integer.valueOf(separated[separated.length-1]));
                }
            }

        });



        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (search_user.getText().toString().length() > 0) {
                    tempUsers.clear();
                    searchUser(search_user.getText().toString());
                    showSearchResult();
                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroup_step2.this);
                    alert.setTitle("Oooops.");
                    alert.setMessage("You have to fill the nickname field");
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            }
        });


        /*addUser = (Button) findViewById(R.id.add_user);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    int id = (int) addUser.getTag();
                    switchUser(id);
                    refreshList();
                }

        });*/


        create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (users.size() > 0){

                    createGroup();

                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroup_step2.this);
                    alert.setTitle(R.string.create_group_problem_title);
                    alert.setMessage(R.string.create_group_problem_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            }
        });
    }



    public void createGroup(){

        new CreateGroup().execute("");
        Intent myIntent = new Intent(CreateGroup_step2.this, GroupActivity.class);
        myIntent.putExtra("id", idGroup); //Optional parameters
        CreateGroup_step2.this.startActivity(myIntent);
        finish();
    }


    public void searchUser(String user){
        final ProgressDialog progressDialog = new ProgressDialog(CreateGroup_step2.this,
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
                AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroup_step2.this);
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
                        final RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);

                        if (tempUsers.size()==0){
                            AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroup_step2.this);
                            alert.setTitle(R.string.no_users_found_title);
                            alert.setMessage(R.string.no_users_found_text);
                            alert.setPositiveButton("OK", null);
                            alert.show();
                            return;
                        }

                        //modify this for item spacing
                        System.out.println(tempUsers.size());
                        int spacing = getResources().getInteger(R.integer.group_creation_recycler_spacing);
                        RecyclerAddUserAdapter ca = new RecyclerAddUserAdapter(tempUsers);
                        rv.setAdapter(ca);
                        rv.addItemDecoration(new VerticalSpacingDecoration(spacing));
                        rv.addItemDecoration(
                                new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(),
                                        R.drawable.item_decorator), spacing));
                        //layout
                        LinearLayoutManager llm = new LinearLayoutManager(CreateGroup_step2.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        rv.setLayoutManager(llm);
            }
        });
    }

    public static void switchUser(int id){

        for (int i = 0; i < tempUsers.size(); i++){
            if (tempUsers.get(i).getId() == id){
                users.add(tempUsers.get(i).getId());
                members.append(id+";");
                return;
            }

        }
    }

    public void addTempUser(final int userId){

        LinearLayout ll = (LinearLayout) findViewById(R.id.userButtons);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,10);
        Button but = new Button(this);
        but.setId(userId);
        but.setLayoutParams(params);
        but.setPadding(25,0,25,0);
        but.setAllCaps(false);
        but.setText(getUser(userId).getName()+" "+getUser(userId).getSurname());
        removeTempUser(userId);
        but.setTextColor(Color.WHITE);
        but.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_remove, 0);
        but.setBackgroundColor(getResources().getColor(R.color.primary));
        ll.addView(but, params);
        Button but1;
        but1 = (Button) findViewById(userId);

        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button but = (Button) findViewById(userId);
                but.setVisibility(View.GONE);
                removeUser(userId);
            }
        });
    }

    public User getUser(int id){
        for (int i = 0; i < users.size(); i++){
            if (tempUsers.get(i).getId() == id)
                return tempUsers.get(i);
        }
        return null;
    }

    public void removeUser(int id){
        for (int i = 0; i < users.size(); i++){
            if (users.get(i) == id)
                users.remove(i);
        }

    }

    public void removeTempUser(int id){
        for (int i = 0; i < tempUsers.size(); i++){
            if (tempUsers.get(i).getId() == id)
                tempUsers.remove(i);
        }

    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://skitalk.altervista.org/php/editGroupPicture.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        //Showing toast message of the response
                        Toast.makeText(CreateGroup_step2.this, s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        //Showing toast
                        Toast.makeText(CreateGroup_step2.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(picture);

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

        ProgressDialog progressDialog = new ProgressDialog(CreateGroup_step2.this,
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

        ProgressDialog progressDialog2 = new ProgressDialog(CreateGroup_step2.this,
                ProgressDialog.STYLE_SPINNER);

        @Override
        protected Boolean doInBackground(final String... arg) {

            for (int i=0; i<users.size();i++){
                System.out.println("Req n. "+i);
                HttpRequest request= new HttpRequest("http://skitalk.altervista.org/php/addUserToGroup.php",
                        "idGroup="+idGroup+"&idUser="+users.get(i));
                Thread tr = new Thread(request);
                tr.start();
                JSONObject resp = request.getResponse();
                System.out.println(resp);

            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success)
                progressDialog2.dismiss();
                //TODO: launch the group activity HERE!
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
