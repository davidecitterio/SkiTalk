package it.polimi.dima.skitalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Davide on 04/01/2017.
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    Context c;

    public MediaButtonIntentReceiver(){
        super ();
    }


    public MediaButtonIntentReceiver(Context c){
        super ();
        this.c = c;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.i ("BUTTON", intentAction.toString() + " happended");
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            Log.i ("BUTTON", "no media button information");
            return;
        }
        KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event.getAction() == KeyEvent.ACTION_UP) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
            return;
        }
        // other stuff you want to do
    }
}


