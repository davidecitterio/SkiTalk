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
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    String name, encodedImage;
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
        encodedImage = encodeToBase64(picture, Bitmap.CompressFormat.JPEG, 50);

        search_user = (EditText) findViewById(R.id.search_user);

        members = (TextView) findViewById(R.id.members);

        ImageView imageView = (ImageView) findViewById(R.id.picture);
        imageView.setImageBitmap(picture);

        TextView nameGroup = (TextView) findViewById(R.id.nameGroup);
        nameGroup.setText(name);



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
    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        return result.toString();
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.URL_SAFE);
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

                        for (int i = 0; i < tempUsers.size(); i++) {

                            //modify this for item spacing
                            System.out.println(tempUsers.size());
                            int spacing = 16;
                            RecyclerAddUserAdapter ca = new RecyclerAddUserAdapter(tempUsers);
                            rv.setAdapter(ca);
                            rv.addItemDecoration(new VerticalSpacingDecoration(spacing));
                            rv.addItemDecoration(
                                    new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.item_decorator), spacing * 2));
                            //layout
                            LinearLayoutManager llm = new LinearLayoutManager(CreateGroup_step2.this);
                            llm.setOrientation(LinearLayoutManager.VERTICAL);
                            rv.setLayoutManager(llm);

                        }
            }
        });
    }

    public static void switchUser(int id){

        for (int i = 0; i < tempUsers.size(); i++){
            if (tempUsers.get(i).getId() == id){
                users.add(tempUsers.get(i).getId());
                if (members.getText().toString().length() > 0)
                    members.append(", "+tempUsers.get(i).getName()+" "+tempUsers.get(i).getSurname()+" ("+tempUsers.get(i).getNickname()+")");
                else
                    members.append(tempUsers.get(i).getName()+" "+tempUsers.get(i).getSurname()+" ("+tempUsers.get(i).getNickname()+")");
                tempUsers.remove(i);
                System.out.println("Switched");
                return;
            }

        }
    }


    private class CreateGroup extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(CreateGroup_step2.this,
                ProgressDialog.STYLE_SPINNER);


        @Override
        protected Boolean doInBackground(String... params) {
            //generate hashMap to store encodedImage and the name
            HashMap<String,String> detail = new HashMap<>();
            detail.put("name", name);
            detail.put("image", encodedImage);
            detail.put("id", String.valueOf(id));


            try {

                /*UploadPicture upload = new UploadPicture("http://skitalk.altervista.org/php/addGroup.php", hashMapToUrl(detail));
                Thread t = new Thread(upload);
                t.start();
                JSONObject response = upload.getResponse();*/

                HttpRequest request= new HttpRequest("http://skitalk.altervista.org/php/addGroup.php",
                        "name="+ URLEncoder.encode(name, "UTF-8")+"&id="+id);
                Thread tr = new Thread(request);
                tr.start();
                JSONObject response = request.getResponse();

                idGroup = response.getInt("id");
                tr.join();
                return true;

            }  catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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
            Intent myIntent = new Intent(CreateGroup_step2.this, HomePage.class);
            myIntent.putExtra("id", id); //Optional parameters
            CreateGroup_step2.this.startActivity(myIntent);
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
