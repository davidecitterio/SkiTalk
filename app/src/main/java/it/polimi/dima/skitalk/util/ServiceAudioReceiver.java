package it.polimi.dima.skitalk.util;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Davide on 10/03/2017.
 */

public class ServiceAudioReceiver extends IntentService {

    AudioTrack track =null;
    byte[] lin = new byte[1024];
    int num = 0;
    private AudioManager m_amAudioManager;
    Socket sock = null;
    String url = "87.4.141.148";
    int port = 4444;
    int userId;

    private AudioIntentReceiver myReceiver;

    public ServiceAudioReceiver()
    {
        super("Service Audio Receiver.");
    }

    public void init(){
        //set user online on server
        Thread t = Utils.setUserOnline(userId, 1);
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Start audio receive service.");
        int maxJitter = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);

        lin = new byte[1024];
        num = 0;
        m_amAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        m_amAudioManager.setSpeakerphoneOn(true);
        myReceiver = new AudioIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
    }


    public void play(){


            try {
                //open socket and send my id
                sock = new Socket(url, port);
                OutputStream out = sock.getOutputStream();
                PrintWriter send = new PrintWriter(out);
                send.write(userId+"\n");
                send.flush();
                System.out.println("Inviato mio id al server: "+userId);


                //loop and catch if something arrive
                while (true) {
                    if ((num = sock.getInputStream().read(lin, 0, 1024)) > 0) {
                        System.out.println("Ricevuto qualcosa: " + num);
                        track.play();
                        track.write(lin, 0, num);
                        track.flush();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Not able to Open serversocket.");
                Toast.makeText(getBaseContext(),"Error: Server is not working, retry later!",
                        Toast.LENGTH_LONG).show();
            }

    }

    @Override
    public void onDestroy()
    {
        //set user offline on server
        Thread t = Utils.setUserOnline(userId, 0);
        /*try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        unregisterReceiver(myReceiver);
        System.out.println("Mi spengo, ciaone.");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        userId = intent.getIntExtra("id", 0);

        init();
        play();
    }

    private class AudioIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        m_amAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        m_amAudioManager.setSpeakerphoneOn(true);
                        System.out.println("Accesi altoparlanti.");
                        break;
                    case 1:
                        m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
                        m_amAudioManager.setSpeakerphoneOn(false);
                        System.out.println("Accese cuffie.");
                        break;
                    default:
                        Log.d("Cuffie", "I have no idea what the headset state is");
                }
            }
        }
    }


}
