package it.polimi.dima.skitalk.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

public class OthersProfile extends AppCompatActivity {
    private DrawerLayout dLayout;
    private OthersProfile thisActivity = this;
    private User user, mainUser;
    private TextView userNameSurname;
    private TextView userNickname;
    private TextView userEmail;
    //private TextView userKm;
    private TextView userAltitude;
    private TextView userSpeed;
    private TextView userStatus;
    private CircleImageView userPicture;
    private boolean status;
    private String lastUpdate, /*km,*/ altitude, speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);
        setToolBar();

        userNameSurname = (TextView) findViewById(R.id.others_profile_name_surname);
        userStatus = (TextView) findViewById(R.id.others_profile_status);
        userNickname = (TextView) findViewById(R.id.others_profile_nickname);
        userEmail = (TextView) findViewById(R.id.others_profile_email);
        //userKm = (TextView) findViewById(R.id.others_profile_km);
        userAltitude = (TextView) findViewById(R.id.others_profile_altitude);
        userSpeed = (TextView) findViewById(R.id.others_profile_speed);
        userPicture = (CircleImageView) findViewById(R.id.others_profile_picture);
        status = getIntent().getBooleanExtra("status", false);
        lastUpdate = getIntent().getStringExtra("lastUpdate");
        //km = getIntent().getStringExtra("km");
        altitude = getIntent().getStringExtra("altitude");
        speed = getIntent().getStringExtra("speed");

        initializeUser();
    }

    //menu a destra
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setToolBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        //drawer icon

        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void initializeUser() {
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        int mainUserId = intent.getIntExtra("mainUserId", 0);

        new OthersProfile.InitializeUser().execute(id, mainUserId);
    }

    private class InitializeUser extends AsyncTask<Integer, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(OthersProfile.this,
                ProgressDialog.STYLE_SPINNER);
        Context c;

        @Override
        protected Boolean doInBackground(Integer... params) {
            synchronized (HomePage.cacheLock) {
                user = new User(params[0], c);
                mainUser = new User(params[1], c, true);
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                loadDrawerHeader();
                userNameSurname.setText(user.getName()+" "+user.getSurname());
                userNickname.setText(user.getNickname());
                userEmail.setText(user.getEmail());
                //userKm.setText(km);
                userAltitude.setText(altitude);
                userSpeed.setText(speed);
                userPicture.setImageBitmap(Utils.getResizedBitmap(user.getPicture(), 256));
                if(status) {
                    userStatus.setTextSize(20);
                    userStatus.setText(getString(R.string.online));
                    userStatus.setTextColor(Color.parseColor("#05D21F"));
                    userPicture.setBorderColor(Color.parseColor("#05D21F"));
                    userPicture.setBorderWidth(8);
                } else {
                    userStatus.setTextSize(15);
                    userStatus.setText("Last seen " + lastUpdate);
                    userPicture.setBorderColor(Color.parseColor("#A1A1A1"));
                }
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
            ((TextView) findViewById(R.id.drawer_name)).setText(mainUser.getName()+" "+mainUser.getSurname());
            ((TextView) findViewById(R.id.drawer_email)).setText(mainUser.getEmail());
            ((CircleImageView) findViewById(R.id.drawer_image)).setImageBitmap(Utils.getResizedBitmap(mainUser.getPicture(), 256));

        }
    }

    private void setNavigationDrawer() {
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int itemId = menuItem.getItemId();
                Intent myIntent;

                if (itemId == R.id.home_page) {
                    myIntent = new Intent(thisActivity, HomePage.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    myIntent.putExtra("id", mainUser.getId());
                    startActivity(myIntent);
                    finish();
                } else if (itemId == R.id.my_profile) {
                    myIntent = new Intent(thisActivity, MyProfile.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    myIntent.putExtra("id", mainUser.getId()); //Optional parameters
                    startActivity(myIntent);
                    finish();
                } else if (itemId == R.id.logout) {
                    myIntent = new Intent(OthersProfile.this, Logout.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    myIntent.putExtra("id", mainUser.getId());
                    startActivity(myIntent);
                    finish();
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
