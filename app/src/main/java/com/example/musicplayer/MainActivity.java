package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean con;

    Button process, takePicture;
    ImageView imageView, hidden;

    private FaceServiceClient faceServiceClient;
    Bitmap mBitmap;
    Boolean ready = false;
    CoordinatorLayout rel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            con=isInternetConnected(MainActivity.this);
        if(!con){

            Toast.makeText(MainActivity.this,"Please Check Your Internet Connection",Toast.LENGTH_SHORT).show();
        }

        //IMPORTANT!!------------------------------------------------------------------------------
        //Replace the below tags <> with your own endpoint and API Subscription Key.
        //For help with this, read the project's README file.
        faceServiceClient = new FaceServiceRestClient("YOUR ENDPOINT", "YOUR SUBSCRIPTION KEY");

        takePicture = findViewById(R.id.takePic);
        imageView = findViewById(R.id.imageView);
        hidden = findViewById(R.id.hidden);
        imageView.setVisibility(View.INVISIBLE);

        process = findViewById(R.id.processClick);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 100);
                }
            }
        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ready) {
                    detectandFrame(mBitmap);
                } else {
                    makeToast("Please take a picture.");
                }
            }
        });
        rel = findViewById(R.id.rel);

    }

    private boolean isInternetConnected(Context ctx) {

        ConnectivityManager connectivityManager=(ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi= connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile= connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifi!= null){
            if(wifi.isConnected()){
                return true;
            }
        }

        if(mobile!=null){
            if(mobile.isConnected()){
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                imageView.setVisibility(View.VISIBLE);
                mBitmap = (Bitmap) data.getExtras().get("data");
                /*Bitmap thumbnail= mBitmap;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);*/
                imageView.setImageBitmap(mBitmap);
                ready = true;
                hidden.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void detectandFrame(final Bitmap mBitmap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream((outputStream.toByteArray()));

        AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {
            ProgressDialog pd = new ProgressDialog(MainActivity.this);

            @Override
            protected Face[] doInBackground(InputStream... inputStreams) {

                publishProgress("Detecting...");
                //This is where you specify the FaceAttributes to detect. You can change this for your own use.
                FaceServiceClient.FaceAttributeType[] faceAttr = new FaceServiceClient.FaceAttributeType[]{
                        FaceServiceClient.FaceAttributeType.HeadPose,
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Emotion,
                        FaceServiceClient.FaceAttributeType.FacialHair
                };

                try {
                    Face[] result = faceServiceClient.detect(inputStreams[0],
                            true,
                            false,
                            faceAttr);

                    if (result == null) {
                        publishProgress("Detection failed. Nothing detected.");
                    }

                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    return result;
                } catch (Exception e) {
                    publishProgress("Detection Failed: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                pd.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                pd.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Face[] faces) {
                pd.dismiss();
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                Gson gson = new Gson();
                String data = gson.toJson(faces);
                if (faces == null || faces.length == 0) {
                    makeToast("No faces detected. Please retake the picture.");
                } else {
                    intent.putExtra("list_faces", data);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    intent.putExtra("image", byteArray);
                    startActivity(intent);
                }

            }
        };
        detectTask.execute(inputStream);
    }

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

}
