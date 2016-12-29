package it.polimi.dima.skitalk;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.json.JSONException;

import it.polimi.dima.model.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // try to download data of user n° 1
        try {

            User davide = new User(1);
            System.out.println("Nickname: "+davide.getNickname());


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
