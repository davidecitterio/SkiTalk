package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;

import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.skitalk.R;

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
        File cacheFile = new File(getApplicationContext().getCacheDir(), "SkiTalkLoginInfo");
        cacheFile.delete();

        cacheFile = new File(getApplicationContext().getCacheDir(), "SkiTalkUserInfo");
        cacheFile.delete();

        cacheFile = new File(getApplicationContext().getCacheDir(), "SkiTalkGroupInfo");
        cacheFile.delete();


        setOffline(id);

        //start login activity
        Intent myIntent = new Intent(Logout.this, Login.class);
        Logout.this.startActivity(myIntent);

    }

    //set user offline
    public void setOffline(int id){
        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/unsetUserOnline.php", "id="+id);
        Thread t = new Thread(request);
        t.start();
    }
}
