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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

public class MyProfile extends AppCompatActivity {
    DrawerLayout dLayout;
    MyProfile thisActivity = this;
    User user;
    TextView userName;
    TextView userSurname;
    TextView userNickname;
    TextView userEmail;
    TextView userPassword;
    CircleImageView userPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        setToolBar();

        userName = (TextView) findViewById(R.id.myprofile_name);
        userSurname = (TextView) findViewById(R.id.myprofile_surname);
        userNickname = (TextView) findViewById(R.id.myprofile_nickname);
        userEmail = (TextView) findViewById(R.id.myprofile_email);
        userPicture = (CircleImageView) findViewById(R.id.myprofile_picture);

        initializeUser();
    }

    //menu a destra
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.myprofile_menu, menu);
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

            case R.id.menu_edit:
                Intent myIntent = new Intent(thisActivity, MyProfileEdit.class);
                myIntent.putExtra("id", user.getId());
                startActivity(myIntent);
        }
        return true;
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

        new MyProfile.InitializeUser().execute(id);
    }

    private class InitializeUser extends AsyncTask<Integer, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(MyProfile.this,
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
                userName.setText(user.getName());
                userSurname.setText(user.getSurname());
                userNickname.setText(user.getNickname());
                userEmail.setText(user.getEmail());
                userPicture.setImageBitmap(Utils.getResizedBitmap(user.getPicture(), 256));
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
                    intent = getIntent();
                    Integer id = intent.getIntExtra("id", 0);
                    myIntent.putExtra("id", id);
                    startActivity(myIntent);
                }
                else if (itemId == R.id.logout) {
                    myIntent = new Intent(MyProfile.this, Logout.class);
                    intent = getIntent();
                    Integer id = intent.getIntExtra("id", 0);
                    myIntent.putExtra("id", id);
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
