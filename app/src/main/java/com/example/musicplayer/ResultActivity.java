package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ResultActivity extends AppCompatActivity {

    TextView smile;
    ImageView imageView;

    static String emotion;

     JcPlayerView jcPlayerView;
     ArrayList<JcAudio> jcAudios = new ArrayList<>();

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result2);

        smile = findViewById(R.id.textSmile2);
        imageView =findViewById(R.id.imgThumb2);
         jcPlayerView=findViewById(R.id.jcplayerView2);
        listView= findViewById(R.id.listView2);


        String data = getIntent().getStringExtra("list_faces");

        Gson gson = new Gson();
        Face[] faces = gson.fromJson(data, Face[].class);


        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap orig = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        if (faces == null) {
            if (data == null) {
                Toast.makeText(getApplicationContext(), "Face array is null", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                detectFrame(faces, orig);
                media(emotion);

            } catch (Exception e) {
                Toast.makeText(ResultActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                jcPlayerView.playAudio(jcAudios.get(position));
                jcPlayerView.setVisibility(View.VISIBLE);
            }
        });
    }

   private void media(String emotion) {


        String whereClause="";
        whereClause="emotion= '"+ emotion+"'";

        DataQueryBuilder queryBuilder=DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Persistence.of(songs.class).find(queryBuilder, new AsyncCallback<List<songs>>() {
            @Override
            public void handleResponse(List<songs> response) {

                ArrayList<songs> tmp= new ArrayList<songs>(response);

                for(songs s: tmp){
                    jcAudios.add(JcAudio.createFromURL(s.getName(), s.getSongUrl()));
                }

                songsAdapter adapter= new songsAdapter( ResultActivity.this, tmp);

                 jcPlayerView.initPlaylist(jcAudios, null);
                listView.setAdapter(adapter);

            }

            @Override
            public void handleFault(BackendlessFault fault) {

                Toast.makeText(ResultActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void detectFrame(Face[] faces, Bitmap orig){

        TreeMap<Double, String> treeMap = new TreeMap<>();
        treeMap.put(faces[0].faceAttributes.emotion.happiness, "Happiness");
        treeMap.put(faces[0].faceAttributes.emotion.anger, "Anger");
        treeMap.put(faces[0].faceAttributes.emotion.disgust, "Disgust");
        treeMap.put(faces[0].faceAttributes.emotion.sadness, "Sadness");
        treeMap.put(faces[0].faceAttributes.emotion.neutral, "Neutral");
        treeMap.put(faces[0].faceAttributes.emotion.surprise, "Surprise");
        treeMap.put(faces[0].faceAttributes.emotion.fear, "Fear");

        ArrayList<Double> arrayList = new ArrayList<>();
        TreeMap<Integer, String> rank = new TreeMap<>();

        int counter = 0;
        for (Map.Entry<Double, String> entry : treeMap.entrySet()) {
            String key = entry.getValue();
            Double value = entry.getKey();
            rank.put(counter, key);
            counter++;
            arrayList.add(value);
        }

        smile.setText("Your Mood: "+rank.get(rank.size() - 1) + ": " + 100 * arrayList.get(rank.size() - 1) + "% " + rank.get(rank.size() - 2) + ": " + 100 * arrayList.get(rank.size() - 2) + "%");

        emotion=rank.get(rank.size()-1);

        FaceRectangle faceRectangle = faces[0].faceRectangle;
        Bitmap bitmap = Bitmap.createBitmap(orig, faceRectangle.left, faceRectangle.top, faceRectangle.width, faceRectangle.height);

        imageView.setImageBitmap(bitmap);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(ResultActivity.this, MainActivity.class));
    }
}
