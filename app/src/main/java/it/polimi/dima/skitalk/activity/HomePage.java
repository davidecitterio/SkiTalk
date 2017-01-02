package it.polimi.dima.skitalk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.model.Group;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.temp.RecyclerTest;
import it.polimi.dima.skitalk.adapter.RecyclerGroupAdapter;

public class HomePage extends AppCompatActivity {
    User user;
    DrawerLayout dLayout;
    HomePage thisActivity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setToolBar();


        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.new_group);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, CreateGroup_step1.class);
                myIntent.putExtra("id", user.getId()); //Optional parameters
                HomePage.this.startActivity(myIntent);
            }
        });


        //TODO : Inserire un loading che termina al caricamento di tutti i dati non sarebbe male

        initializeUser();
        showGroups();


    }

    private void setToolBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        //drawer icon
        ab.setHomeAsUpIndicator(R.mipmap.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    //menu a destra
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String btnName = null;
        setNavigationDrawer();

        switch(itemId) {
            /*
            case R.id.menu_settings:
                btnName = "Settings";
                break;
            */
            // Android home
            case android.R.id.home: {
                dLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }

        /*Snackbar.make(layout, "Button " + btnName,
                Snackbar.LENGTH_SHORT).show();*/
        return true;
    }

    private void setNavigationDrawer() {
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Fragment frag = null;
                int itemId = menuItem.getItemId();
                Intent myIntent;

                if (itemId == R.id.my_profile) {
                    myIntent = new Intent(thisActivity, MyProfile.class);
                    myIntent.putExtra("id", user.getId()); //Optional parameters
                    HomePage.this.startActivity(myIntent);
                }
                else if (itemId == R.id.logout) {
                    myIntent = new Intent(HomePage.this, Logout.class);
                    myIntent.putExtra("id", user.getId()); //Optional parameters
                    HomePage.this.startActivity(myIntent);
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

    private void initializeUser(){
        Intent intent = getIntent();
        Integer id = intent.getIntExtra("id", 0);

        user = new User(id);
    }

    private void showGroups(){
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        ArrayList<Group> groups = user.getGroups();
                        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);

                        for (int i=0; i< groups.size();i++){
                            //USE THIS TO RETRIVE BITMAP IMAGE (NON HO IDEA DI COME STAMPARLA A VIDEO XD)
                            //groups.get(i).getPicture();

                            //Da implementare: quando uno clicca su un gruppo si apre l'activity corrispondete.
                            // all'activity si passa l'id del gruppo e l'id dell'utente

                            RecyclerGroupAdapter ca = new RecyclerGroupAdapter(groups);
                            rv.setAdapter(ca);
                            //layout
                            LinearLayoutManager llm = new LinearLayoutManager(HomePage.this);
                            llm.setOrientation(LinearLayoutManager.VERTICAL);
                            rv.setLayoutManager(llm);

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
    } catch (Exception e) {
            e.printStackTrace();
        }
    }
}