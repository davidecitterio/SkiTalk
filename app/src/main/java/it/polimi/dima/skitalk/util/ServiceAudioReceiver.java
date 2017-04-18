package it.polimi.dima.skitalk.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.Nullable;

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
    AudioManager am;
    Socket sock = null;
    String url = "87.2.99.216";
    int port = 4444;
    int userId;

    public ServiceAudioReceiver()
    {
        super("Service Audio Receiver.");
    }

    public void init(){
        System.out.println("Start audio receive service.");
        int maxJitter = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter*50, AudioTrack.MODE_STREAM);

        lin = new byte[1024];
        num = 0;
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        am.setSpeakerphoneOn(true);

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
                        track.write(lin, 0, num);
                        track.play();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Not able to Open serversocket.");
            }

    }

    @Override
    public void onDestroy()
    {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Mi spengo, ciaone.");


    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        userId = intent.getIntExtra("id", 0);
        init();
        play();
    }


}
