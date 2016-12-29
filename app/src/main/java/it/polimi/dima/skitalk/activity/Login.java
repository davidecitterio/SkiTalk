package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.skitalk.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Davide on 29/12/2016.
 */

public class Login extends Activity {

    Button login,signin;
    EditText username,password;

    TextView tx1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!checkAlreadyLoggedIn())
            doLogin();
        else
            setLogin();
    }

    public void doLogin(){
        login = (Button)findViewById(R.id.login_button);
        signin = (Button)findViewById(R.id.signin_button);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // INSERISCI QUI IL CERCHIETTO DI LOADING
                System.out.println("cliccato");
                //http request to the server
                String user = username.getText().toString();
                String pass = password.getText().toString();

                HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/login.php",
                        "email="+user+"&password="+pass);
                Thread t = new Thread(request);
                t.start();
                JSONObject response = request.getResponse();

                try {
                    // if wrong credentials
                    if(response.getInt("id") == -1) {
                        System.out.println("wrong credentials");
                        Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show();
                    }
                    // if correct credentials
                    else{
                        Toast.makeText(getApplicationContext(), "Redirecting...",Toast.LENGTH_SHORT).show();
                        System.out.println("L'id dell'user è: "+response.getInt("id"));

                        saveLogin(response.getInt("id"));

                        //start homepage activity
                        Intent myIntent = new Intent(Login.this, HomePage.class);
                        myIntent.putExtra("id", response.getInt("id")); //Optional parameters
                        Login.this.startActivity(myIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start sign in activity
                Intent myIntent = new Intent(Login.this, SignIn.class);
                Login.this.startActivity(myIntent);
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
            //start homepage activity
            Intent myIntent = new Intent(Login.this, HomePage.class);
            myIntent.putExtra("id", Integer.parseInt(buffer.toString())); //Optional parameters
            Login.this.startActivity(myIntent);

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


}
