package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.temp.UploadPicture;

import static android.content.ContentValues.TAG;

/**
 * Created by Davide on 30/12/2016.
 */

public class CreateGroup_step2 extends Activity{
    Button create, addUser;
    String name;
    Bitmap picture;
    int numOfUsers = 0;
    String pictureName;
    int id, groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_step_2);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        name = intent.getStringExtra("name");
        System.out.println(id);
        System.out.println(name);
        picture = (Bitmap) intent.getParcelableExtra("picture");

        ImageView imageView = (ImageView) findViewById(R.id.picture);
        imageView.setImageBitmap(picture);

        TextView nameGroup = (TextView) findViewById(R.id.nameGroup);
        nameGroup.setText(name);

        //addUser = (Button) findViewById(R.id.add_user);
        /*addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewUser();
            }
        });*/

        create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numOfUsers > -1){

                    createGroup();

                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroup_step2.this);
                    alert.setTitle(R.string.create_group_problem_title);
                    alert.setMessage(R.string.create_group_problem_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            }
        });
    }



    public void createGroup(){

        Integer picName = 1000 + (int)(Math.random() * ((5000 - 1000) + 1));
        pictureName = picName.toString();
        new Upload(picture, pictureName).execute();
    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    //async task to upload image
    private class Upload extends AsyncTask<Void,Void,String> {
        private Bitmap image;
        private String name;


        public Upload(Bitmap image,String name){
            this.image = image;
            this.name = name;
        }

        @Override
        protected String doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //compress the image to jpg format
            image.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            /*
            * encode image to base64 so that it can be picked by saveImage.php file
            * */
            String encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);

            //generate hashMap to store encodedImage and the name
            HashMap<String,String> detail = new HashMap<>();
            detail.put("name", name);
            detail.put("image", encodeImage);

            try{
                //convert this HashMap to encodedUrl to send to php file
                String dataToSend = hashMapToUrl(detail);
                //make a Http request and send data to saveImage.php file


                UploadPicture upload = new UploadPicture("http://skitalk.altervista.org/php/uploadGroupPicture.php",dataToSend);
                Thread t = new Thread(upload);
                t.start();


                //return the response
                return "ok";

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"ERROR  "+e);
                return null;
            }
        }



        @Override
        protected void onPostExecute(String s) {
            //show image uploaded
            Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();
        }
    }

}
