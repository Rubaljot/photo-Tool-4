package com.example.editphoto.editphoto;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EditPhotoActivity extends AppCompatActivity {

    //reference to ImageView And Buttons
ImageView imvphoto;
Button btgallery;
Button btcamera;
    LinearLayout frameslayout;
ArrayList<Integer> al;
Bitmap camerabitmap=null;
Bitmap gallertbitmap=null;
Bitmap framebitmap=null;
    Bitmap out=null;
String imagefrom="";
boolean isedit;
TextView tvsaved;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);
tvsaved=(TextView) findViewById(R.id.tvsaved);
        //memory to ImageView and Buttons
        imvphoto=(ImageView)findViewById(R.id.imvphoto);
        btgallery=(Button)findViewById(R.id.btgallery);
        btcamera=(Button)findViewById(R.id.btcamera);

        //bind buttons with listeners
        btgallery.setOnClickListener(gallerylistener);
        btcamera.setOnClickListener(cameralistener);


    }


//gallery open listener
    View.OnClickListener gallerylistener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent in = new Intent((Intent.ACTION_PICK));
                // Filter for image type
                in.setType("image/*");
                startActivityForResult(in,11);//here we send 11 as request code to camera ,you can send any other number
            }
            catch (Exception ex){
                ex.printStackTrace();
            }

        }
    };


//camera open listener
    View.OnClickListener cameralistener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(in,10);//here we send 10 as request code to gallery,you can send any other number
            }
            catch (Exception ex){
                ex.printStackTrace();
            }

        }
    };





//after you open camera or gallery it will send you back bitmap or uri.
    //bitmap from camera
    //uri from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//onActivityResult  is function in which response came from camera or gallery
        if(requestCode==10)  //back from camera   //if we receive 10 as request code sent by us that means response from camera
        {
            if(resultCode==RESULT_OK)///R
            {

                //bitmap is actually a data structure which store image in form of array of bytes
                Bitmap bmp = (Bitmap) (data.getExtras().get("data"));

                camerabitmap=bmp;
                //set bitmap to ImageView
                imvphoto.setImageBitmap(bmp);
                imagefrom="camera";
            }
        }
        else if(requestCode==11)  //back from gallery   //if we receive 11 as request code sent by us that means response from gallery
        {
            if(resultCode==RESULT_OK)
            {


                //uri is actually address of image  which is already stored in our phone
                Uri uri = data.getData();
                getRealPathFromURI(getApplicationContext(),uri);
                try {
                    gallertbitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imagefrom="gallery";
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //set uri to ImageView
                imvphoto.setImageURI(uri);
            }
        }

    }



    public  void add(Bitmap bmpframe,Bitmap bmpimg){
        try {
            Bitmap frame = bmpframe;
//            Bitmap urImage = BitmapFactory.decodeResource(getResources(),
//                    R.drawable.four);//edit
//            frame = BitmapFactory.decodeResource(getResources(),
//                    R.drawable.frame1);
            Bitmap urImage = bmpimg;//edit
            out = combineImages(frame, urImage);
            imvphoto.setImageBitmap(out);

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public Bitmap combineImages(Bitmap frame, Bitmap image) {

        Bitmap cs = null;
        Bitmap rs = null;

        rs = Bitmap.createScaledBitmap(frame, image.getWidth(),
                image.getHeight(), true);

        cs = Bitmap.createBitmap(rs.getWidth(), rs.getHeight(),
                Bitmap.Config.RGB_565);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(image, 0, 0, null);
        comboImage.drawBitmap(rs, 0, 0, null);

        if (rs != null) {
            rs.recycle();
            rs = null;
        }
        Runtime.getRuntime().gc();

        return cs;
    }




    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            return path;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-Edited" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            tvsaved.setText("Image saved at \n\n"+Environment.getExternalStorageDirectory().toString()+File.separator+file.getName());
            Toast.makeText(getApplicationContext(),"Image Saved ",Toast.LENGTH_SHORT).show();
            //f1=new File(root+File.separator+fname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mytoolbarmenu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       if (item.getItemId() == R.id.m1) {


           if(isedit){
               long time= System.currentTimeMillis();
               saveImage(out,time+"");
               item.setTitle("Edit");
               isedit=false;
               imagefrom="";
               gallertbitmap=null;
               camerabitmap=null;
               out=null;
               framebitmap=null;
               frameslayout.setVisibility(View.INVISIBLE);
               imvphoto.setImageResource(R.drawable.photoicon);
           }
           else{
               if(imagefrom.equals("")){
                   Toast.makeText(getApplicationContext(),"first choose image ",Toast.LENGTH_SHORT).show();
               }
               else {
                   tvsaved.setText("");
                   makeframes();
                   item.setTitle("Save");
                   isedit = true;
               }
           }
       }
        return true;
    }


    public void makeframes(){
        try{
            frameslayout = (LinearLayout) (findViewById(R.id.frameslayout));
            frameslayout.removeAllViews();
            frameslayout.setVisibility(View.VISIBLE);
            al=new ArrayList<>();
            al.add(R.drawable.frame1);
            al.add(R.drawable.frame3);
            al.add(R.drawable.frame4);
            al.add(R.drawable.frame5);
            al.add(R.drawable.frame6);
            al.add(R.drawable.frame7);
            al.add(R.drawable.frame8);
            al.add(R.drawable.frame13);
            al.add(R.drawable.frame11);
            al.add(R.drawable.frame2);
            al.add(R.drawable.frame12);
            al.add(R.drawable.frame14);
            al.add(R.drawable.frame16);
            al.add(R.drawable.frame19);
            al.add(R.drawable.frame25);
            for (int m = 0; m < al.size(); m++) {
                Log.d("ALLOOP",al.get(m)+"");
                final ImageView imageView = new ImageView(getApplicationContext());
                imageView.setLayoutParams(new LinearLayout.LayoutParams(180, 180));
                ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(imageView.getLayoutParams());
                marginParams.setMargins(5, 2, 25, 2);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
                imageView.setLayoutParams(layoutParams);

                Picasso.get().load(al.get(m)).resize(180,180).centerInside().into(imageView);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                 final int index=m;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     framebitmap=   BitmapFactory.decodeResource(getResources(),al.get(index));
                     if(imagefrom.equals("gallery")){
                     add(framebitmap,gallertbitmap);}
                     else if(imagefrom.equals("camera")){
                         add(framebitmap,camerabitmap);
                     }

                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        frameslayout.addView(imageView);

                    }
                });
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
