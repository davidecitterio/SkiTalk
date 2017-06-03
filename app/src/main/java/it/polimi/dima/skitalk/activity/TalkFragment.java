package it.polimi.dima.skitalk.activity;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import it.polimi.dima.skitalk.R;


public class TalkFragment extends Fragment{

    Button rec;

    AudioRecord record =null;

    boolean isPlaying=false;

    Socket sendAudio;

    String url = "95.233.40.129";
    int port = 4544;

    int idGroup;
    int idUser;
    Thread t;
    boolean quit = false;

    CoordinatorLayout snackbarCoordinatorLayout;

    public TalkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        idGroup = args.getInt("groupId");
        idUser = args.getInt("userId");

        init();
        t = new Thread() {
            @Override
            public void run() {
                try {
                    recordAndPlay();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error: server doesn't responding.");
                    showSnackBar();
                }

            }
        };
        t.start();

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

        snackbarCoordinatorLayout = (CoordinatorLayout)view.findViewById(R.id.snackbarCoordinatorLayout);

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
        isPlaying=false;
    }

    private void init() {
        int min = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);
    }

    private void recordAndPlay() throws IOException {
        byte[] lin = new byte[1024];
        boolean socketAlreadyOpen = false;
        int msg;

        while (!quit){
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
                    send.write(idUser+" "+idGroup+"\n");
                    send.flush();
                    BufferedReader br = new BufferedReader(new InputStreamReader(sendAudio.getInputStream()));
                    String ack = br.readLine();
                    System.out.println("Receive "+ack);
                    if (ack.equals("no")){
                        Vibrator v0 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        v0.vibrate(200);

                        isPlaying = false;
                        socketAlreadyOpen = false;
                        System.out.println("Channel busy, retry later..");
                        break;
                    }

                    else if (ack.equals("ok")){
                        socketAlreadyOpen = true;
                        System.out.println("Open Socket. "+idUser+" "+idGroup+"\n");
                    }
                }


                if ((msg = record.read(lin, 0, 1024)) > 0) {
                    System.out.println("Try to send.\n");
                    sendAudio.getOutputStream().write(lin, 0, msg);
                    sendAudio.getOutputStream().flush();
                }
            }
        }

        return;
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
        quit = true;
        System.out.println("Chiudo record.");
        super.onDestroy();
    }

}