package it.polimi.dima.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import it.polimi.dima.skitalk.util.Utils;

/**
 * Created by Max on 11/03/2017.
 */

public class ImageUploader implements Runnable {

    private Bitmap userPicture;
    private Context c;
    private String address;
    private Map<String, String> parameters;
    Response.Listener<String> listener;

    public ImageUploader(Context c, Bitmap userPicture, String address, Map<String, String> parameters, Response.Listener<String> listener) {
        this.userPicture = userPicture;
        this.c = c;
        this.address = address;
        this.parameters = parameters;
        this.listener = listener;
    }

    @Override
    public void run() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, address,
                listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        //Showing toast
                        //Toast.makeText(MyProfileEdit.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = Utils.getStringImage(userPicture);

                //Adding parameters
                parameters.put("picture", image);

                //returning parameters
                return parameters;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(c);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}
