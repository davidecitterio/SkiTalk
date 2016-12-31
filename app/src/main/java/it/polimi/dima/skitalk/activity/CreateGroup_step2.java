package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.temp.RecyclerTest;
import it.polimi.dima.skitalk.temp.RecyclerTestAdapter;
import it.polimi.dima.skitalk.temp.UploadPicture;

/**
 * Created by Davide on 30/12/2016.
 */

public class CreateGroup_step2 extends Activity{
    Button create, search, addUser;
    EditText search_user;
    String name, encodedImage;
    Bitmap picture;
    int numOfUsers = 0;
    int id;

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
        encodedImage = encodeToBase64(picture, Bitmap.CompressFormat.JPEG, 50);

        search_user = (EditText) findViewById(R.id.search_user);

        ImageView imageView = (ImageView) findViewById(R.id.picture);
        imageView.setImageBitmap(picture);

        TextView nameGroup = (TextView) findViewById(R.id.nameGroup);
        nameGroup.setText(name);



        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUser(search_user.getText().toString());
            }
        });

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

        //generate hashMap to store encodedImage and the name
        HashMap<String,String> detail = new HashMap<>();
        detail.put("name", name);
        detail.put("image", encodedImage);
        detail.put("id", String.valueOf(id));


        try {
            UploadPicture upload = new UploadPicture("http://skitalk.altervista.org/php/addGroup.php", hashMapToUrl(detail));
            Thread t = new Thread(upload);
            t.start();
            JSONObject response = upload.getResponse();
            try {
                System.out.println(response.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        return result.toString();
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.URL_SAFE);
    }

    public  void searchUser(String user){
        final ProgressDialog progressDialog = new ProgressDialog(CreateGroup_step2.this,
                ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();

        HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/searchUser.php", "nickname="+user);
        Thread t = new Thread(request);
        t.start();
        JSONArray response = request.getArrayResponse();

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        final List<RecyclerTest> countryList = new ArrayList<RecyclerTest>();

        String members = new String();

        for (int i=0; i<response.length(); i++){
            RecyclerTest temp = null;
            try {
                temp = new RecyclerTest(response.getJSONObject(i).getString("nickname"),  members);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            countryList.add(temp);
        }

        progressDialog.dismiss();

        RecyclerTestAdapter ca = new RecyclerTestAdapter(countryList);
        rv.setAdapter(ca);
        //layout
        LinearLayoutManager llm = new LinearLayoutManager(CreateGroup_step2.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

    }


}
