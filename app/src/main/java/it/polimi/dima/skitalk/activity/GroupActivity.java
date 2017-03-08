package it.polimi.dima.skitalk.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.model.Group;
import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.skitalk.MediaButtonIntentReceiver;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

public class GroupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Integer userId, groupId;
    private Group group;
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", 0);
        groupId = intent.getIntExtra("groupId", 0);

        bundle.putInt("groupId", groupId);
        bundle.putInt("userId",userId);

        loadGroup();

        System.out.println("name is "+group.getName());

        setupToolbar();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        ((AudioManager)getSystemService(AUDIO_SERVICE)).registerMediaButtonEventReceiver(new ComponentName(this,MediaButtonIntentReceiver.class));
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TalkFragment(), getString(R.string.tab_talk));
        adapter.addFragment(new MapFragment(), getString(R.string.tab_map));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);

        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragment.setArguments(bundle);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    //menu a destra
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        setupMenu(menu.findItem(R.id.mute));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home:
                goBackToHome();
                return true;

            case R.id.mute:
                setMute(item);
                return true;

            case R.id.settings:

                return true;

            case R.id.leave_group:

                leaveGroup();
        }

        /*Snackbar.make(layout, "Button " + btnName,
                Snackbar.LENGTH_SHORT).show();*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goBackToHome();
    }

    private void loadGroup() {
        try {
            group = new Group(groupId, getApplicationContext(), false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);;
        int savedActiveGroupID = sharedPref.getInt(getString(R.string.saved_active_group_id), -1);
        System.out.println("active "+savedActiveGroupID);
        if(savedActiveGroupID == group.getId())
            group.setActive(true);
        else
            group.setActive(false);
    }

    private void setMute(MenuItem muteItem) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);;
        SharedPreferences.Editor editor = sharedPref.edit();
        if(group.isActive()) {
            editor.putInt(getString(R.string.saved_active_group_id), -1);
            editor.commit();
            group.setActive(false);
        } else {
            editor.putInt(getString(R.string.saved_active_group_id), group.getId());
            editor.commit();
            group.setActive(true);
        }
        //update menu
        setupMenu(muteItem);
        updateActiveIcon();
    }

    private void setupMenu(MenuItem muteItem) {
        if(group.isActive())
            muteItem.setTitle(getResources().getString(R.string.mute));
        else
            muteItem.setTitle(getResources().getString(R.string.unmute));
    }

    private void setupToolbar() {
        //load notification icon if group is active
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int savedActiveGroupID = sharedPref.getInt(getString(R.string.saved_active_group_id), -1);

        toolbar = (Toolbar) findViewById(R.id.group_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("  "+group.getName());
        toolbar.setLogo(new BitmapDrawable(getApplicationContext().getResources(), Utils.getResizedBitmap(group.getPicture(), getResources().getInteger(R.integer.group_bar_picture_dimension))));

        updateActiveIcon();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void updateActiveIcon() {
        if(group.isActive())
            ((ImageView) findViewById(R.id.group_toolbar_active)).setImageDrawable(getResources().getDrawable(R.drawable.ic_active_group));
        else
            ((ImageView) findViewById(R.id.group_toolbar_active)).setImageDrawable(null);
    }

    private void goBackToHome() {
        Intent myIntent = new Intent(GroupActivity.this, HomePage.class);
        myIntent.putExtra("id", userId);
        GroupActivity.this.startActivity(myIntent);
    }

    private void leaveGroup(){
        new AlertDialog.Builder(this)
                .setTitle("Leaving "+group.getName())
                .setMessage("Do you really want to leave this group?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/" + "leaveGroup.php", "idUser=" + userId + "&idGroup="+groupId);
                        Thread t = new Thread(request);
                        t.start();

                        group.clearCache();
                        goBackToHome();

                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
}
