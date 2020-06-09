package com.example.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

public class songsAdapter extends ArrayAdapter<songs> {

    private ArrayList<songs> songs;
    private Context context;

    public songsAdapter(@NonNull Context context, ArrayList<songs> list) {
        super(context, R.layout.row_layout, list);
        this.context= context;
        this.songs= list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.row_layout, parent, false);

        TextView tvsong= convertView.findViewById(R.id.tvSong);

        tvsong.setText(songs.get(position).getName());

        return convertView;
    }
}

