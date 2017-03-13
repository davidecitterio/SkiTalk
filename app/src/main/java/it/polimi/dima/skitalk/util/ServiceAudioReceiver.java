package it.polimi.dima.skitalk.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Davide on 10/03/2017.
 */

public class ServiceAudioReceiver extends IntentService {

    //AudioManager am = null;
    AudioTrack track =null;
    byte[] lin = new byte[1024];
    int num = 0;
    AudioManager am;
    ServerSocket receiveAudio;
    Socket sock = null;

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


        System.out.println("Prima track play.");
        track.play();
        System.out.println("Dopo track play.");
    }


    public void play(){


            try {
                receiveAudio = new ServerSocket(8086);
                while (true) {
                    num = 0;
                    sock = receiveAudio.accept();
                    System.out.println("Open serversocket.");
                    if ((num = sock.getInputStream().read(lin, 0, 1024)) > 0) {
                        System.out.println("Accetto connessioni da altri.");
                        track.write(lin, 0, num);
                        System.out.println("Ricevuto qualcosa: " + num);
                    }
                    sock.close();
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
            receiveAudio.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Mi spengo, ciaone.");


    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        init();
        play();
    }


}
