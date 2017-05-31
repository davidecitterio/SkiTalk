package it.polimi.dima.skitalk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

/**
 * Created by Davide on 30/12/2016.
 */

public class CreateGroup_step1 extends Activity {
    private final Activity thisActivity = this;
    Button next;
    CircleImageView selectpicture;
    EditText name;
    Bitmap picture;
    String picturePath;
    Uri selectedImage;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_step_1);

        Intent intent = getIntent();
        final Integer id = intent.getIntExtra("id", 0);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitleTextColor(Color.WHITE);
        tb.setTitle(getString(R.string.new_group));

        selectpicture = (CircleImageView) findViewById(R.id.group_picture);
        selectpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.pickImage(thisActivity);
            }
        });

        name = (EditText) findViewById(R.id.name);

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((name.getText().toString().length() > 0) && (picture != null)){

                    Intent myIntent = new Intent(CreateGroup_step1.this, CreateGroup_step2.class);
                    Bundle extras = new Bundle();
                    extras.putInt("id",id);
                    extras.putString("name",name.getText().toString());
                    extras.putParcelable("picture",picture);
                    myIntent.putExtras(extras);
                    CreateGroup_step1.this.startActivity(myIntent);
                    finish();
                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroup_step1.this);
                    alert.setTitle(R.string.signin_problem_title);
                    alert.setMessage(R.string.signin_problem_text);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
               System.out.println("error retriving picture from gallery.");
                return;
            }
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                picture = BitmapFactory.decodeStream(inputStream);

                picture = Utils.getResizedBitmap(picture, 300);

                selectpicture.setImageBitmap(picture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }
}
