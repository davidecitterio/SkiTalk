package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

public class MyProfileEdit extends AppCompatActivity {
    private DrawerLayout dLayout;
    private MyProfileEdit thisActivity = this;
    private User user;
    private EditText userNameView, userSurnameView, userNicknameView, userEmailView, userPasswordView;
    private String userName, userSurname, userNickname, userEmail;
    private boolean userPictureEdit;
    private CircleImageView userPictureView;
    private Bitmap userPicture;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_edit);
        setToolBar();

        userPictureEdit = false;

        userNameView = (EditText) findViewById(R.id.myprofile_edit_name);
        userSurnameView = (EditText) findViewById(R.id.myprofile_edit_surname);
        userNicknameView = (EditText) findViewById(R.id.myprofile_edit_nickname);
        userEmailView = (EditText) findViewById(R.id.myprofile_edit_email);
        userPasswordView = (EditText) findViewById(R.id.myprofile_edit_password);
        userPictureView = (CircleImageView) findViewById(R.id.myprofile_edit_picture);
        save = (Button)findViewById(R.id.save_button);

        userPasswordView.setText("default");

        userPictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.pickImage(thisActivity);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEdit();
                Intent myIntent = new Intent(thisActivity, MyProfile.class);
                myIntent.putExtra("id", user.getId());
                startActivity(myIntent);
            }
        });
        initializeUser();
    }

    private void saveEdit() {
        HttpRequest request1 = null, request2 = null;
        boolean updateStrings = !nothingHasBeenModified();
        //request for strings
        if(updateStrings) {
            String params = "idUser="+user.getId()+"&name="+userNameView.getText()+"&surname="+userSurnameView.getText()+
                    "&nickname="+userNicknameView.getText()+"&email="+userEmailView.getText();
            if(!(userPasswordView.getText().toString()).equals("default"))
                params = params + "&password="+userPasswordView.getText();
            request1 = new HttpRequest("http://skitalk.altervista.org/php/editUser.php", params);
            Thread t1 = new Thread(request1);
            t1.start();
        }

        //TODO caricamento immagine
        /*QUESTO POI ANDRA' TOLTO*/userPictureEdit = false;

        //request for picture
        if(userPictureEdit)
            uploadImage();

        if(userPictureEdit) {
            //delete old image
            File cacheFile = new File(thisActivity.getCacheDir(), ""+user.getPictureURL().hashCode());
            cacheFile.delete();
            //save new image
            JSONObject response = request2.getResponse();
            String pictureUrl = null;
            try {
                pictureUrl = response.getString("picture");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.putBitmapInDiskCache(thisActivity, pictureUrl, userPicture);
            //save user file
            JSONObject user = request1.getResponse();
            try {
                user.put("picture", pictureUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            User.saveUserInfo(user, thisActivity);
        } else if(updateStrings) {
            JSONObject user = request1.getResponse();
            User.saveUserInfo(user, thisActivity);
        }
    }

    private boolean nothingHasBeenModified() {
        return userName.equals(userNameView.getText().toString()) && userSurname.equals(userSurnameView.getText().toString()) &&
                userNickname.equals(userNicknameView.getText().toString()) && userEmail.equals(userEmailView.getText().toString()) &&
                (userPasswordView.getText().toString()).equals("default");
    }

    private void uploadImage(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://skitalk.altervista.org/php/editUserPicture.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        //Showing toast
                        Toast.makeText(MyProfileEdit.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = Utils.getStringImage(userPicture);

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("picture", image);
                params.put("name", "pic_"+user.getId());
                params.put("id", String.valueOf(user.getId()));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                System.out.println("error retriving picture from gallery.");
                return;
            }
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                userPicture = BitmapFactory.decodeStream(inputStream);
                userPicture = Utils.getResizedBitmap(userPicture, 300);
                userPictureView.setImageBitmap(userPicture);
                userPictureEdit = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    private void setToolBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        //drawer icon
        ab.setHomeAsUpIndicator(R.mipmap.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void initializeUser() {
        Intent intent = getIntent();
        Integer id = intent.getIntExtra("id", 0);

        new MyProfileEdit.InitializeUser().execute(id);
    }

    private class InitializeUser extends AsyncTask<Integer, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(MyProfileEdit.this,
                ProgressDialog.STYLE_SPINNER);
        Context c;

        @Override
        protected Boolean doInBackground(Integer... params) {
            user = new User(params[0], c, true);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                loadDrawerHeader();
                userName = user.getName();
                userNameView.setText(userName);
                userSurname = user.getSurname();
                userSurnameView.setText(userSurname);
                userNickname = user.getNickname();
                userNicknameView.setText(userNickname);
                userEmail = user.getEmail();
                userEmailView.setText(userEmail);
                userPicture = user.getPicture();
                userPictureView.setImageBitmap(Utils.getResizedBitmap(userPicture, 256));
                progressDialog.dismiss();
            }
            else
                System.out.println("Nooooooooo");
        }

        @Override
        protected void onPreExecute() {
            c = getApplicationContext();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        private void loadDrawerHeader() {
            ((TextView) findViewById(R.id.drawer_name)).setText(user.getName()+" "+user.getSurname());
            ((TextView) findViewById(R.id.drawer_email)).setText(user.getEmail());
            ((CircleImageView) findViewById(R.id.drawer_image)).setImageBitmap(Utils.getResizedBitmap(user.getPicture(), 256));

        }
    }

    //menu a destra
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String btnName = null;
        setNavigationDrawer();

        switch(itemId) {
            // Android home
            case android.R.id.home: {
                dLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return true;
    }

    private void setNavigationDrawer() {
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int itemId = menuItem.getItemId();
                Intent intent, myIntent;

                if (itemId == R.id.home_page) {
                    myIntent = new Intent(thisActivity, HomePage.class);
                    myIntent.putExtra("id", user.getId());
                    startActivity(myIntent);
                }
                else if (itemId == R.id.logout) {
                    myIntent = new Intent(MyProfileEdit.this, Logout.class);
                    myIntent.putExtra("id", user.getId());
                    startActivity(myIntent);
                } else {
                    dLayout.closeDrawer(GravityCompat.START);
                }

                /*
                if (frag != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.frame, frag);
                    transaction.commit();
                    dLayout.closeDrawers();
                    return true;
                }*/

                return false;
            }
        });
    }
}
