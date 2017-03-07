package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;

import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.skitalk.R;

/**
 * Created by Davide on 29/12/2016.
 */

public class SignIn extends Activity {

    Button signin;
    EditText nickname, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        setSignIn();
    }

    public void setSignIn(){
        signin = (Button)findViewById(R.id.signin_button);
        email = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        nickname = (EditText)findViewById(R.id.nickname);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (email.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0
                        && nickname.getText().toString().trim().length() > 0) {


                    HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/addUser.php",
                            "email=" + email.getText().toString() + "&nickname=" + nickname.getText().toString() + "&password=" + password.getText().toString());
                    Thread t = new Thread(request);
                    t.start();
                    JSONObject response = request.getResponse();

                    try {
                        saveLogin(response.getInt("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //start homepage activity
                    Intent myIntent = new Intent(SignIn.this, HomePage.class);
                    try {
                        myIntent.putExtra("id", response.getInt("id")); //Optional parameters
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SignIn.this.startActivity(myIntent);

                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(SignIn.this);
                    alert.setTitle(R.string.signin_problem_title);
                    alert.setMessage(R.string.signin_problem_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            }
        });

    }

    // save login info of this user
    public void saveLogin(int id){
        String filename = "SkiTalkLoginInfo";
        String string = String.valueOf(id);
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, getApplicationContext().MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
