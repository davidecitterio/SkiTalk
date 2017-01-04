package it.polimi.dima.skitalk.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.polimi.dima.skitalk.R;


public class TalkFragment extends Fragment{

    Button rec;

    public TalkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


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
    }

    public void onUp(){
        rec.setBackgroundResource(R.drawable.ic_talk);
        Vibrator v1 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v1.vibrate(50);
    }

}
