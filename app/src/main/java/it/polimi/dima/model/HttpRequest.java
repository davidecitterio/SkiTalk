package it.polimi.dima.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Davide on 17/12/2016.
 */

public class HttpRequest implements Runnable{

    private String targetURL;
    private String urlParameters;
    JSONObject res = null;
    private boolean ready = false;
    JSONArray jsonArr;


    public HttpRequest(String url, String par){
        targetURL = url;
        urlParameters = par;
    }


    @Override
    public void run() {
        URL url;
        String response="";
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(targetURL+"?"+urlParameters);

            System.out.println("Request to: "+targetURL+"?"+urlParameters);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();

            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                response += current;

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        try {

            jsonArr = new JSONArray(response);  //<<< convert to jsonarray
            res = jsonArr.getJSONObject(0);
            ready = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public JSONObject getResponse(){
        while(!ready);
        return res;
    }


    public JSONArray getArrayResponse(){
        while(!ready);
        return jsonArr;
    }


}
