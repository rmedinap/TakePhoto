package pe.hypergis.takephoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btnTakePhoto;
    Button btnUploadPhoto;
    ImageView imgPhoto;
    Bitmap imageBitmap;
    String imageFileName;

    private static final String DATA_DIRECTORY = Environment.getExternalStorageDirectory().getPath();
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        btnUploadPhoto = (Button) findViewById(R.id.btnUploadPhoto);
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageBitmap != null) {
                    //Codifica la imagen con Base64
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    //Se ejecuta en segundo plano para no colgar la aplicacion
                    new UploadTask(MainActivity.this).execute(encodedImage);
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        int permission1 = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if(permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }

        if (permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
            imageFileName = "PIC_" + timeStamp + ".jpg";

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri photoURI =  FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        createImageFile());
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
            }else{
                Uri uri = Uri.parse("file:///sdcard/" + imageFileName);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            }


            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String vFileName = "PIC_" + timeStamp + ".jpg";
        File file = new File(DATA_DIRECTORY, vFileName);
        imageFileName = file.getAbsolutePath();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String pPath = "";
            if(!imageFileName.equals("")) {
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    pPath = imageFileName;
                }else {
                    File file = new File(DATA_DIRECTORY, imageFileName);
                    pPath = file.getAbsolutePath();
                }
            }else{
                Uri selectedImageUri = data.getData();
                String uriImg = getRealPathFromURI(selectedImageUri);
                pPath = uriImg;
            }

            try {
                imageBitmap = BitmapFactory.decodeFile(pPath);
                imgPhoto.setImageBitmap(imageBitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getApplicationContext().getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
