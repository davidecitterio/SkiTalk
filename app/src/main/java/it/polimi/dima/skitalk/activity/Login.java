package it.polimi.dima.skitalk.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.FbUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by Davide on 29/12/2016.
 */

public class Login extends Activity {

    Button login;
    LoginButton fb_login;
    TextView signin;
    EditText username,password;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);

        //REQUEST PERMISSION FOR LOCATION IN MAPS
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);

        if (!checkAlreadyLoggedIn())
            doLogin();
        else
            setLogin();
    }

    public void doLogin(){
        login = (Button)findViewById(R.id.login_button);
        fb_login = (LoginButton)findViewById(R.id.login_button_fb);
        signin = (TextView) findViewById(R.id.signin_link);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Async mAuthTask;
                mAuthTask = new Async();
                mAuthTask.execute(username.getText().toString(), password.getText().toString());

            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start sign in activity
                Intent myIntent = new Intent(Login.this, SignIn.class);
                Login.this.startActivity(myIntent);
                finish();
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        System.out.println("login Success"+loginResult.getAccessToken().getUserId());

                        String accessToken = loginResult.getAccessToken().getToken();
                        //FbUtil.saveAccessToken(accessToken);

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject jsonObject,
                                                            GraphResponse response) {

                                        // Getting FB User Data
                                        Bundle facebookData = getFacebookData(jsonObject);

                                        FacebookLoginAsync mAuthTask;
                                        mAuthTask = new FacebookLoginAsync();
                                        mAuthTask.execute(facebookData);

                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,first_name,last_name,middle_name,email");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Login attempt cancelled.");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Login attempt failed.");
                        deleteAccessToken();
                    }
                });
    }

    // check if a user is already logged in this device
    public boolean checkAlreadyLoggedIn(){
        File file = new File(getApplicationContext().getCacheDir(), "SkiTalkLoginInfo");
        if (file.exists())
            return true;
        else
            return false;
    }

    // retrive data of already logged in user
    public void setLogin() {
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "SkiTalkLoginInfo"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            System.out.println("the id is: "+buffer);


            /*
            //start receive audio service
            Intent serviceIntentAudio = new Intent(getApplicationContext(),ServiceAudioReceiver.class);
            serviceIntentAudio.putExtra("id", Integer.parseInt(buffer.toString()) );
            startService(serviceIntentAudio);*/

            //start homepage activity
            Intent myIntent = new Intent(Login.this, HomePage.class);
            myIntent.putExtra("id", Integer.parseInt(buffer.toString())); //Optional parameters
            Login.this.startActivity(myIntent);
            finish();

            Log.d(TAG, buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    // save login info of this user
    public void saveLogin(int id){
        String content = String.valueOf(id);
        File file;
        FileOutputStream outputStream;
        try {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(getCacheDir(), "SkiTalkLoginInfo");

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class FacebookLoginAsync extends AsyncTask<Bundle, Void, Boolean> {
        final ProgressDialog progressDialog = new ProgressDialog(Login.this,
                ProgressDialog.STYLE_SPINNER);
        boolean result = false;
        int id = -1;

        @Override
        protected void onPreExecute(){

            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.authenticating));
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(Bundle... params) {
                System.out.println("Inizio login con fb..");
                //http request to the server
                String email = params[0].getString("email");

                System.out.println("Email è: " + email);

                HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/checkFacebook.php",
                        "email=" + email);
                Thread t = new Thread(request);
                t.start();
                JSONObject response = request.getResponse();

                try {
                    // if wrong credentials
                    if (response.getInt("id") == -1) {
                        System.out.println("Non ancora registrato");

                        //registro nel sistema
                        HttpRequest requestSignIn = new HttpRequest("http://skitalk.altervista.org/php/addUserFb.php",
                                "email=" + email + "&nickname=" + params[0].getString("first_name")
                                         + params[0].getString("last_name") + "&password=" + "password" + "&time=" + System.currentTimeMillis()
                                + "&name="+ params[0].getString("first_name") + "&surname=" + params[0].getString("last_name"));

                        Thread tSignIn = new Thread(requestSignIn);
                        tSignIn.start();
                        JSONObject responseSignIn = requestSignIn.getResponse();

                        try {
                            saveLogin(responseSignIn.getInt("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        result = true;
                        id = responseSignIn.getInt("id");
                    }
                    // if correct credentials
                    else {

                        System.out.println("L'id dell'user è: " + response.getInt("id"));

                        saveLogin(response.getInt("id"));

                        result = true;
                        id = response.getInt("id");

                    }

                    //load new picture
                    HttpRequest postPicture = new HttpRequest("http://skitalk.altervista.org/php/editFbUserPicture.php",
                            "&idUser=" + id +
                                    "&picture=" + params[0].getString("pictureUrl"));

                    Thread tpostPicture = new Thread(postPicture);
                    tpostPicture.start();
                    JSONObject postPictureReply = postPicture.getResponse();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            afterLogin(result, id);
            progressDialog.dismiss();
        }


    }



    public class Async extends AsyncTask<String, Void, Boolean> {
        final ProgressDialog progressDialog = new ProgressDialog(Login.this,
                ProgressDialog.STYLE_SPINNER);
        boolean result = false;
        int id = -1;

        @Override
        protected void onPreExecute(){

            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.authenticating));
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... params) {
            if (params[0].trim().length() > 0 && params[1].trim().length() > 0) {


                System.out.println("cliccato");
                //http request to the server
                String user = params[0];
                String pass = params[1];

                HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/login.php",
                        "email=" + user + "&password=" + pass);
                Thread t = new Thread(request);
                t.start();
                JSONObject response = request.getResponse();

                try {
                    // if wrong credentials
                    if (response.getInt("id") == -1) {
                        System.out.println("wrong credentials");
                        result = false;
                        return true;
                    }
                    // if correct credentials
                    else {

                        System.out.println("L'id dell'user è: " + response.getInt("id"));

                        saveLogin(response.getInt("id"));

                        result = true;
                        id = response.getInt("id");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                result = false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            afterLogin(result, id);
            progressDialog.dismiss();
        }


    }

    void afterLogin(boolean result, int id){
        if (result){
            //start homepage activity
            Intent myIntent = new Intent(Login.this, HomePage.class);
            myIntent.putExtra("id", id); //Optional parameters
            Login.this.startActivity(myIntent);
            finish();
        }

        else{
            AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
            alert.setTitle(R.string.wrong_credentials_title);
            alert.setMessage(R.string.wrong_credentials_text);
            alert.setPositiveButton("OK", null);
            alert.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void deleteAccessToken() {
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    //User logged out
                    FbUtil.clearToken();
                    LoginManager.getInstance().logOut();
                }
            }
        };
    }


    //extract data from facebook
    private Bundle getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();

        try {
            String id = object.getString("id");
            URL profile_pic;
            try {
                profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?type=normal");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("pictureUrl", profile_pic.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("middle_name"))
                bundle.putString("middle_name", object.getString("middle_name"));

            /*
            FbUtil.saveFacebookUserInfo(object.getString("first_name"),
                    object.getString("last_name"),object.getString("email"),
                    object.getString("gender"), profile_pic.toString());*/

        } catch (Exception e) {
            Log.d(TAG, "BUNDLE Exception : "+e.toString());
        }

        return bundle;
    }
}

