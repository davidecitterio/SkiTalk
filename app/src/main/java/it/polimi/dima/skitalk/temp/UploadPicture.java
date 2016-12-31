package it.polimi.dima.skitalk.temp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


    /**
     * Created by Davide on 17/12/2016.
     */

    public class UploadPicture implements Runnable{

        private String targetURL;
        private String urlParameters;
        JSONObject res = null;
        private boolean ready = false;
        JSONArray jsonArr;


        public UploadPicture(String url, String par){
            targetURL = url;
            urlParameters = par;
        }


        @Override
        public void run() {
            URL url;

            try {
                URL obj = new URL(targetURL);
                HttpURLConnection con = null;

                    con = (HttpURLConnection) obj.openConnection();


                //add reuqest header
                con.setRequestMethod("POST");

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + targetURL);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());
            } catch (IOException e) {
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

