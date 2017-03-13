package it.polimi.dima.skitalk.activity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.net.Socket;

import it.polimi.dima.skitalk.R;


public class TalkFragment extends Fragment{

    Button rec;

    AudioRecord record =null;

    boolean isPlaying=false;

    Socket sendAudio;


    public TalkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init();
        (new Thread() {
            @Override
            public void run() {
                try {
                    recordAndPlay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_talk, container, false);

        rec = (Button) view.findViewById(R.id.rec);

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

        return view;
    }

    public void onDown(){
        rec.setBackgroundResource(R.drawable.ic_talk_on);
        Vibrator v0 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v0.vibrate(50);
        record.startRecording();
        isPlaying=true;
    }

    public void onUp(){
        rec.setBackgroundResource(R.drawable.ic_talk);
        Vibrator v1 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v1.vibrate(50);
        record.stop();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isPlaying=false;
    }

    private void init() {
        int min = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 16000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);
    }

    private void recordAndPlay() throws IOException {
        byte[] lin = new byte[1024];
        while (true){
            while (isPlaying) {
                sendAudio = new Socket("127.0.0.1", 8086);
                sendAudio.getOutputStream().write(lin, 0, record.read(lin, 0, 1024));
                sendAudio.getOutputStream().flush();
                sendAudio.getOutputStream().close();
                sendAudio.close();
            }
        }
    }


}
