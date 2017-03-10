package it.polimi.dima.skitalk.util;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

import it.polimi.dima.model.HttpRequest;

/**
 * Created by Davide on 09/03/2017.
 */

public class ServiceUpdate extends IntentService {

    int userId;
    double lat = 0, lon = 0;

    public ServiceUpdate()
    {
        super("Update Service");
    }

    @Override
    protected void onHandleIntent(Intent i)
    {
        userId = i.getIntExtra("id", 0);

        while (true) {
            HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/editCoords.php",
                    "idUser=" + userId + "&lat=" + getLat() + "&lon=" + getLon());
            Thread tr = new Thread(request);
            tr.start();

            System.out.println("Coord Service is running\n");
            SystemClock.sleep(100000);
        }

    }

    @Override
    public void onDestroy()
    {
        System.out.println("Mi spengo, ciaone.");
    }

    public double getLat(){
        //TODO: retrive current latitude
        return ++lat;
    }

    public double getLon(){
        //TODO: retrive current longitude
        return ++lon;
    }

}
