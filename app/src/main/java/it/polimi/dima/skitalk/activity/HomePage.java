package it.polimi.dima.skitalk.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import it.polimi.dima.model.Group;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.adapter.RecyclerGroupAdapter;
import it.polimi.dima.skitalk.util.DividerItemDecoration;
import it.polimi.dima.skitalk.util.RecyclerItemListener;
import it.polimi.dima.skitalk.util.VerticalSpacingDecoration;

public class HomePage extends AppCompatActivity implements SearchView.OnQueryTextListener {
    User user;
    DrawerLayout dLayout;
    HomePage thisActivity = this;
    RecyclerGroupAdapter ca;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setToolBar();


        //REQUEST PERMISSION FOR LOCATION IN MAPS
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);

        //create groups button
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.new_group);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, CreateGroup_step1.class);
                myIntent.putExtra("id", user.getId()); //Optional parameters
                HomePage.this.startActivity(myIntent);
            }
        });

        //loading groups
        initializeUser();
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
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String btnName = null;
        setNavigationDrawer();

        switch (itemId) {
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
                } else if (itemId == R.id.logout) {
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

    private void initializeUser() {
        Intent intent = getIntent();
        Integer id = intent.getIntExtra("id", 0);

        new InitializeUser().execute(id);
    }

    private class InitializeUser extends AsyncTask<Integer, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(HomePage.this,
                ProgressDialog.STYLE_SPINNER);
        Context c;

        @Override
        protected Boolean doInBackground(Integer... params) {
            /*SharedPreferences sharedPref = thisActivity.getPreferences(Context.MODE_PRIVATE);
            int savedUserId = sharedPref.getInt(getString(R.string.saved_user_id), -1);

            if(savedUserId == -1 || savedUserId != params[0]) {
                //retrieve user info*/
                user = new User(params[0], c);
                /*//save user info in local storage
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.saved_user_id), user.getId());
                editor.putString(getString(R.string.saved_user_name), user.getName());
                editor.putString(getString(R.string.saved_user_surname), user.getSurname());
                editor.putString(getString(R.string.saved_user_nickname), user.getNickname());
                editor.putString(getString(R.string.saved_user_email), user.getEmail());
                editor.putString(getString(R.string.saved_user_picture_url), user.getPictureURL());
                editor.commit();
            } else {

            }*/

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                final ArrayList<Group> groups = user.getGroups();
                RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
                final SharedPreferences sharedPref = thisActivity.getPreferences(Context.MODE_PRIVATE);
                //modify this for item spacing
                int spacing = 96;
                ca = new RecyclerGroupAdapter(groups);
                rv.setAdapter(ca);
                rv.addItemDecoration(new VerticalSpacingDecoration(spacing));
                rv.addItemDecoration(
                        new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(),
                                R.drawable.item_decorator), spacing));
                //layout
                LinearLayoutManager llm = new LinearLayoutManager(HomePage.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                rv.setLayoutManager(llm);
                rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), rv,
                        new RecyclerItemListener.RecyclerTouchListener() {
                            public void onClickItem(View v, int position) {
                                Intent myIntent = new Intent(HomePage.this, GroupActivity.class);
                                Bundle extras = new Bundle();
                                extras.putInt("userId",user.getId());
                                extras.putInt("groupId",groups.get(position).getId());
                                System.out.println("passo id group: "+groups.get(position).getId());
                                myIntent.putExtras(extras);
                                HomePage.this.startActivity(myIntent);
                            }

                            public void onClickSwitch(View v, int position) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                //set switch checked and save id
                                SwitchCompat swtch  = (SwitchCompat) v.findViewById(R.id.groupSwitch);
                                if(swtch.isChecked()) {
                                    swtch.setChecked(false);
                                    groups.get(position).setActive(false);
                                    editor.putInt(getString(R.string.saved_active_group_id), -1);
                                    editor.commit();
                                } else {
                                    swtch.setChecked(true);
                                    groups.get(position).setActive(true);
                                    editor.putInt(getString(R.string.saved_active_group_id), groups.get(position).getId());
                                    editor.commit();
                                }
                                //set other groups non active
                                for(int i = 0; i < groups.size(); i++) {
                                    if(i != position)
                                        groups.get(i).setActive(false);
                                    //System.out.println("   " + i + " : " + groups.get(i).isActive());
                                }
                                //set other visible switches unchecked
                                RecyclerView rec = (RecyclerView) v.getParent();
                                System.out.println("pos: "+position+" on "+rec.getChildCount()+", checked: "+swtch.isChecked());
                                if(swtch.isChecked())
                                    for(int i = 0; i < groups.size(); i++)
                                        //change the state of visible switches
                                        if(i != position) {
                                            RecyclerGroupAdapter.MyViewHolder child = (RecyclerGroupAdapter.MyViewHolder) rec.findViewHolderForAdapterPosition(i);
                                            if(child != null) {
                                                SwitchCompat sw = child.swtch;
                                                if (sw.isChecked()) {
                                                    sw.setChecked(false);
                                                }
                                            } else {
                                                //notify item changed for non visible switches
                                                ca.notifyItemChanged(i);
                                            }
                                        }
                            }
                        }, getScreenWidth()-192));
                //show which group is active
                int savedActiveGroupID = sharedPref.getInt(getString(R.string.saved_active_group_id), -1);
                if(savedActiveGroupID != -1) {
                    for(Group g : groups) {
                        if(g.getId() == savedActiveGroupID)
                            g.setActive(true);
                    }
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

        private int getScreenWidth() {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            return width;
        }
    }

    private void showGroups() {

        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        ArrayList<Group> groups = user.getGroups();
                        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);

                        for (int i = 0; i < groups.size(); i++) {

                            //modify this for item spacing
                            int spacing = 16;
                            RecyclerGroupAdapter ca = new RecyclerGroupAdapter(groups);
                            rv.setAdapter(ca);
                            rv.addItemDecoration(new VerticalSpacingDecoration(spacing));
                            rv.addItemDecoration(
                                    new DividerItemDecoration(ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.item_decorator), spacing * 2));
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


    @Override
    public boolean onQueryTextChange(String query) {
        ca.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

}