package it.polimi.dima.skitalk.util;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Davide on 10/03/2017.
 */

public class ServiceAudioReceiver extends IntentService {

    static final int frequency = 16000;
    static final int channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isPlaying;
    int playBufSize;
    ServerSocket connfd;
    AudioTrack audioTrack;

    public ServiceAudioReceiver()
    {
        super("Audio Receiver Service");
    }

    @Override
    protected void onHandleIntent(Intent in)
    {

        playBufSize=AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(1f, 1f);

        System.out.println("Audio Receiver Service is started.");


            byte[] buffer = new byte[playBufSize];

                try {
                    connfd = new ServerSocket(8086);
                    System.out.println("ServerSocket opened.");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent()
                            .setAction("tw.rascov.MediaStreamer.ERROR")
                            .putExtra("msg", e.toString());
                    getApplication().sendBroadcast(intent);
                    return;
                }
                audioTrack.play();
                isPlaying = true;
                Socket soc = null;
                try {
                    soc = connfd.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (isPlaying) {
                    int readSize = 0;
                    try {
                        readSize = soc.getInputStream().read(buffer);
                        System.out.println("Receiving data..");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent()
                                .setAction("tw.rascov.MediaStreamer.ERROR")
                                .putExtra("msg", e.toString());
                        getApplication().sendBroadcast(intent);
                        break;
                    }
                    audioTrack.write(buffer, 0, readSize);
                }

                try { connfd.close(); }
                catch (Exception e) { e.printStackTrace(); }



    }

    @Override
    public void onDestroy()
    {
        System.out.println("Mi spengo, ciaone.");
        audioTrack.stop();
        isPlaying = false;
    }



}
