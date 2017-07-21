package it.polimi.dima.skitalk.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.model.Group;
import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.MediaButtonIntentReceiver;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;


public class GroupActivity extends AppCompatActivity implements MediaButtonIntentReceiver.Delegate{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Integer userId, groupId;
    private Group group;
    //private TalkFragment talkFragment;
    private MembersFragment membersFragment;
    private MapFragment mapFragment;
    private Bundle bundle = new Bundle();

    //attributes for talk functionality
    private Button rec;
    private AudioRecord record =null;
    private boolean isPlaying=false;
    private Socket sendAudio;
    private final String url = "87.4.149.39";
    private final int port = 4544;
    private CoordinatorLayout snackbarCoordinatorLayout;
    RecordAndPlay recordAndPlay;
    MediaButtonIntentReceiver mb = new MediaButtonIntentReceiver();
    boolean firstClick = true;

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

        setupTalkFunctionality();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //talkFragment = new TalkFragment();
        membersFragment = new MembersFragment();
        mapFragment = new MapFragment();
        //adapter.addFragment(talkFragment, getString(R.string.tab_talk));
        adapter.addFragment(membersFragment, getString(R.string.tab_members));
        adapter.addFragment(mapFragment, getString(R.string.tab_map));
        viewPager.setAdapter(adapter);
    }



    private class ViewPagerAdapter extends FragmentPagerAdapter {
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


            case R.id.leave_group:

                leaveGroup();
        }

        /*Snackbar.make(layout, "Button " + btnName,
                Snackbar.LENGTH_SHORT).show();*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void loadGroup() {
        synchronized (HomePage.cacheLock) {
            try {
                group = new Group(groupId, getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            int savedActiveGroupID = sharedPref.getInt(getString(R.string.saved_active_group_id), -1);
            System.out.println("active " + savedActiveGroupID);
            if (savedActiveGroupID == group.getId())
                group.setActive(true);
            else
                group.setActive(false);
        }
    }

    private void setMute(MenuItem muteItem) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);;
        SharedPreferences.Editor editor = sharedPref.edit();
        if(group.isActive()) {
            //modify on server
            HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/setActiveGroup.php", "idUser="+userId+"&idGroup=-1");
            Thread t = new Thread(groupsRequest);
            t.start();
            //modify locally
            editor.putInt(getString(R.string.saved_active_group_id), -1);
            editor.commit();
            group.setActive(false);
        } else {
            //modify on server
            HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/setActiveGroup.php", "idUser="+userId+"&idGroup="+groupId);
            Thread t = new Thread(groupsRequest);
            t.start();
            //modify locally
            editor.putInt(getString(R.string.saved_active_group_id), group.getId());
            editor.commit();
            group.setActive(true);
        }
        //update menu
        setupMenu(muteItem);
        updateActiveIcon();
        updateButton();
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
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.putExtra("id", userId);
        GroupActivity.this.startActivity(myIntent);
        finish();
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

                        group.clearGroupCache();
                        goBackToHome();

                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public Group getGroup() {
        return group;
    }

    public void passUserToMap(List<User> membersList) {
        mapFragment.setMembersList(membersList);
    }

    /************************************
     **** TALK FUNCTIONALITY METHODS ****
     ************************************/

    private void setupTalkFunctionality() {
        init();
        rec = (Button) findViewById(R.id.rec);
        setupButton();

        snackbarCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.snackbarCoordinatorLayout);
    }

    private void setupButton() {
        rec.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    onDown();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    onUp();
                }
                return true;
            }

        });
        updateButton();
    }

    private void updateButton() {
        if(group.isActive()) {
            rec.setEnabled(true);
            rec.setBackgroundResource(R.drawable.ic_talk);
            ((LinearLayout) findViewById(R.id.buttonSectionLayout)).setBackgroundColor(getResources().getColor(R.color.primary_light));
        } else {
            rec.setEnabled(false);
            rec.setBackgroundResource(R.drawable.ic_talk_inactive);
            ((LinearLayout) findViewById(R.id.buttonSectionLayout)).setBackgroundColor(getResources().getColor(R.color.divider));
        }
    }

    public void onDown(){
        tone(1000, 100, 1.0);
        rec.setBackgroundResource(R.drawable.ic_talk_on);
        Vibrator v0 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v0.vibrate(50);
        HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/setGroupBusy.php", "idUser="+userId+"&idGroup="+groupId);
        Thread t = new Thread(groupsRequest);
        t.start();
        record.startRecording();
        isPlaying=true;
    }

    public void onUp(){
        tone(1000, 100, 1.0);
        rec.setBackgroundResource(R.drawable.ic_talk);
        Vibrator v1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v1.vibrate(50);

        HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/unsetUserTalking.php", "idUser="+userId+"&idGroup="+groupId);
        Thread t = new Thread(groupsRequest);
        t.start();

        record.stop();
        isPlaying=false;
    }

    @Override
    public void onMediaButtonSingleClick() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (firstClick){
                    tone(1000, 100, 1.0);
                    rec.setBackgroundResource(R.drawable.ic_talk_on);
                    Vibrator v0 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v0.vibrate(50);

                    HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/setGroupBusy.php", "idUser="+userId+"&idGroup="+groupId);
                    Thread t = new Thread(groupsRequest);
                    t.start();

                    record.startRecording();
                    isPlaying=true;
                    firstClick = false;
                }
                else{
                    tone(1000, 100, 1.0);
                    rec.setBackgroundResource(R.drawable.ic_talk);
                    Vibrator v1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v1.vibrate(50);

                    HttpRequest groupsRequest = new HttpRequest("http://skitalk.altervista.org/php/unsetUserTalking.php", "idUser="+userId+"&idGroup="+groupId);
                    Thread t = new Thread(groupsRequest);
                    t.start();

                    record.stop();
                    isPlaying=false;
                    firstClick = true;
                }
            }
        });
    }

    @Override
    public void onMediaButtonDoubleClick() {

    }

    public static void tone(int hz, int msecs, double vol){
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 150);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, hz);
    }

    private void init() {
        int min = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);
        recordAndPlay =new RecordAndPlay();
        recordAndPlay.start();
    }


    public void showSnackBar(){
        Snackbar.make(snackbarCoordinatorLayout, "Error: Server not working.", Snackbar.LENGTH_LONG)
                .setAction("Retry", null)
                .setActionTextColor(Color.RED)
                .show();
    }

    @Override
    public void onDestroy() {
        record.release();
        recordAndPlay.stopRunning();
        System.out.println("Chiudo record.");
        super.onDestroy();
    }

    @Override
    public void onResume(){
        mb.delegate = (MediaButtonIntentReceiver.Delegate) this;
        mb.register(this);
        super.onResume();
    }


    class RecordAndPlay extends Thread {
        boolean running = false;
        byte[] lin = new byte[1024];
        boolean socketAlreadyOpen = false;
        int msg;


        public void run() {
            running = true;
            while(running){
                try {
                    if (socketAlreadyOpen && !isPlaying) {
                        sendAudio.getOutputStream().close();
                        sendAudio.close();
                        socketAlreadyOpen = false;
                        System.out.println("Close socket.");
                    }
                while (isPlaying) {

                    if (!socketAlreadyOpen) {
                        sendAudio = new Socket(url, port);
                        OutputStream out = sendAudio.getOutputStream();
                        PrintWriter send = new PrintWriter(out);
                        send.write(userId+" "+groupId+"\n");
                        send.flush();
                        BufferedReader br = new BufferedReader(new InputStreamReader(sendAudio.getInputStream()));
                        String ack = br.readLine();
                        System.out.println("Receive "+ack);
                        if (ack.equals("no")){
                            Vibrator v0 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v0.vibrate(200);
                            isPlaying = false;
                            socketAlreadyOpen = false;
                            System.out.println("Channel busy, retry later..");
                            break;
                        }

                        else if (ack.equals("ok")){
                            socketAlreadyOpen = true;
                            System.out.println("Open Socket. "+userId+" "+groupId+"\n");
                        }
                    }


                    if ((msg = record.read(lin, 0, 1024)) > 0) {
                        System.out.println("Try to send.\n");
                        sendAudio.getOutputStream().write(lin, 0, msg);
                        sendAudio.getOutputStream().flush();
                    }
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Thread spento.");
        }

        public void stopRunning() {
            running = false;
        }
    }



}
