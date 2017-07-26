package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.login.LoginManager;

import java.io.File;

import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

/**
 * Created by Davide on 29/12/2016.
 */


public class Logout extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        Integer value = intent.getIntExtra("id", 0); //if it's a string you stored.

        doLogout(value);
    }

    public void doLogout(int id){
        File cacheDirectory = getApplicationContext().getCacheDir();
        Utils.deleteDir(cacheDirectory);

        Utils.setUserOnline(id, 0);

        try {
            LoginManager.getInstance().logOut();}
        catch (Exception e) {
            System.out.println("Eccezione logout facebook!");
        }

        //start login activity
        Intent myIntent = new Intent(Logout.this, Login.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Logout.this.startActivity(myIntent);
        finish();
    }
}
