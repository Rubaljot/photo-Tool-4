package com.editphoto;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.editphoto.pack1.pack2.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //reference to ImageView And Buttons
    CropImageView imvphoto;
    Button btgallery;
    Button btcamera;
    Button btcrop;
    LinearLayout frameslayout;
    ArrayList<Integer> al;
    Bitmap camerabitmap=null;
    Bitmap gallertbitmap=null;
    Bitmap framebitmap=null;
    Bitmap out=null;
    String imagefrom="";
    boolean isedit;
    TextView tvsaved;
    ImageView imvdefaultl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvsaved=(TextView) findViewById(R.id.tvsaved);
        //memory to ImageView and Buttons
        imvphoto=(CropImageView)findViewById(R.id.imvphoto);
        imvdefaultl=(ImageView)findViewById(R.id.imvdefault);

        imvphoto.setCropShape(CropImageView.CropShape.OVAL);

        btgallery=(Button)findViewById(R.id.btgallery);
        btcamera=(Button)findViewById(R.id.btcamera);
        btcrop=(Button)findViewById(R.id.btcrop);
btcrop.setVisibility(View.INVISIBLE);
        //bind buttons with listeners
        btgallery.setOnClickListener(gallerylistener);
        btcamera.setOnClickListener(cameralistener);


        //this function is used to android version
        checkAndroidVersion();


    }


    //gallery open listener
    View.OnClickListener gallerylistener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {

                Intent in = new Intent(Intent.ACTION_PICK);
                // Filter for image type
                in.setType("image/*");
                startActivityForResult(in,11);//here we send 11 as request code to gallery ,you can send any other number
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
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, 10);
//                Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(in,10);
                //here we send 10 as request code to camera,you can send any other number
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
                imvdefaultl.setVisibility(View.INVISIBLE);
                btcrop.setVisibility(View.VISIBLE);
                String imgPath = data.getStringExtra(CameraActivity.EXTRA_IMAGE_PATH);
                Toast.makeText(getApplicationContext(),imgPath,Toast.LENGTH_SHORT).show();
                imvphoto.setImageBitmap(BitmapHelper.decodeSampledBitmap(imgPath, imvphoto.getWidth(), imvphoto.getHeight()));
                //bitmap is actually a data structure which store image in form of array of bytes
                //Bitmap bmp = (Bitmap) (data.getExtras().get("data"));
                Uri uri=Uri.parse(imgPath);
               // Bitmap bmp = StringToBitMap(imgPath);
                try{
                    File imgFile = new  File(imgPath);
                    if(imgFile.exists()){
                        Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                   // Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    camerabitmap = bmp;
                    //set bitmap to ImageView
                    // imvphoto.setImageBitmap(bmp);
                    imagefrom = "camera";}
                }
                catch(Exception ex) {

                }
            }
        }
        else if(requestCode==11)  //back from gallery   //if we receive 11 as request code sent by us that means response from gallery
        {
            if(resultCode==RESULT_OK)
            {
                imvdefaultl.setVisibility(View.INVISIBLE);
                btcrop.setVisibility(View.VISIBLE);
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
                imvphoto.setImageUriAsync(uri);
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
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            btcrop.setVisibility(View.INVISIBLE);
            imvphoto.clearImage();
            imvdefaultl.setVisibility(View.VISIBLE);
            tvsaved.setText("Image saved at \n\n"+Environment.getExternalStorageDirectory().toString()+File.separator+file.getName());
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
                Bitmap cropped = imvphoto.getCroppedImage(500, 500);
                saveImage(cropped,time+"");
                imvdefaultl.setVisibility(View.VISIBLE);
                frameslayout.setVisibility(View.INVISIBLE);
                item.setTitle("Edit");
                isedit=false;
                imagefrom="";
                gallertbitmap=null;
                camerabitmap=null;
                out=null;
                framebitmap=null;
                imvphoto.setImageResource(R.drawable.photoicon);
            }
            else{
                if(imagefrom.equals("")){
                    Toast.makeText(getApplicationContext(),"first choose image ",Toast.LENGTH_SHORT).show();
                }
                else {
                    tvsaved.setText("");
                    btcrop.setVisibility(View.INVISIBLE);
                    makeframes();
                    item.setTitle("Save");
                    isedit = true;
                }
            }
        }
        else if(item.getItemId() == R.id.m2){
            Intent in=new Intent(MainActivity.this,MainActivity.class);
            startActivity(in);

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
//                Log.d("ALLOOP",al.get(m)+"");
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






    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //this function is used to check runtime permissions of camera/write to external storage /read from external storage
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }




    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        int wtite = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (wtite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 10);
            return false;
        }
        return true;
    }




    //when we on permission response will come in this function like OnActivityResult
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//        Log.d("in activity on request", "Permission callback called-------");
        switch (requestCode) {
            case 10: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                        Log.d("in activity on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
//                        Log.d("in activity on request", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera and Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(getApplicationContext(), "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }



    //this is dialog to on permission
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getApplicationContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    public Bitmap StringToBitMap(String image){
        try{
            byte [] encodeByte=Base64.decode(image,Base64.DEFAULT);

            InputStream inputStream  = new ByteArrayInputStream(encodeByte);
            Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }


    public static class Log {

        private static final String TAG = "CameraSpike";

        public static void d(String msg) {
            d(msg, null);
        }

        public static void d(String msg, Throwable e) {
            android.util.Log.d(TAG, Thread.currentThread().getName() + "| " + msg, e);
        }

        public static void i(String msg) {
            i(msg, null);
        }

        public static void i(String msg, Throwable e) {
            android.util.Log.i(TAG, Thread.currentThread().getName() + "| " + msg, e);
        }

        public static void e(String msg) {
            e(msg, null);
        }

        public static void e(String msg, Throwable e) {
            android.util.Log.e(TAG, Thread.currentThread().getName() + "| " + msg, e);
        }

        public static void v(String msg) {
            android.util.Log.v(TAG, Thread.currentThread().getName() + "| " + msg);
        }

        public static void w(String msg) {
            android.util.Log.w(TAG, Thread.currentThread().getName() + "| " + msg);
        }

    }
    public void cropsave(View view){
        long time= System.currentTimeMillis();
        Bitmap cropped = imvphoto.getCroppedImage(500, 500);
        saveImage(cropped,time+"");
    }
}
