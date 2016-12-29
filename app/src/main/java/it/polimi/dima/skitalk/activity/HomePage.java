package it.polimi.dima.skitalk.activity;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.temp.RecyclerTest;
import it.polimi.dima.skitalk.temp.RecyclerTestAdapter;

public class HomePage extends AppCompatActivity {
    DrawerLayout dLayout;
    HomePage thisActivity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setToolBar();
        //test code for RecyclerView
        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        List<RecyclerTest> countryList = new ArrayList<RecyclerTest>();
        RecyclerTest temp = new RecyclerTest("Italy", 60);
        countryList.add(temp);
        temp = new RecyclerTest("Germany", 100);
        countryList.add(temp);
        RecyclerTestAdapter ca = new RecyclerTestAdapter(countryList);
        rv.setAdapter(ca);
        //layout
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
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
                Intent intent;

                if (itemId == R.id.my_profile) {
                    intent = new Intent(thisActivity, MyProfile.class);
                    startActivity(intent);
                }
                else if (itemId == R.id.logout) {
                    //TODO: insert user id
                    Intent myIntent = new Intent(HomePage.this, Logout.class);
                    myIntent.putExtra("id", 1); //Optional parameters
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
}
