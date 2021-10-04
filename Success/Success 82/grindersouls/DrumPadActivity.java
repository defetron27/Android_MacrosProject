package com.deffe.macros.grindersouls;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DrumPadActivity extends AppCompatActivity
{
    ArrayList<String> Musics;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drum_pad);

        Musics = new ArrayList<>();

        Musics.add("Albatraoz");

        RecyclerView DrumPadMusicsRecyclerView = findViewById(R.id.drum_pad_musics_recycler_view);
        DrumPadMusicsRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        DrumPadMusicsAdapter drumPadMusicsAdapter = new DrumPadMusicsAdapter();
        DrumPadMusicsRecyclerView.setAdapter(drumPadMusicsAdapter);
    }

    public class DrumPadMusicsAdapter extends RecyclerView.Adapter<DrumPadMusicsAdapter.DrumPadMusicViewHolder>
    {
        @NonNull
        @Override
        public DrumPadMusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drum_pad_music_items,parent,false);

            return new DrumPadMusicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final DrumPadMusicViewHolder holder, int position)
        {
            final String game = Musics.get(position);

            holder.MusicIcon.setImageDrawable(getResources().getDrawable(R.drawable.drum_pad_musics));

            holder.MusicName.setText(game);

            holder.v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivity(new Intent(DrumPadActivity.this,PlayDrumPadMusicsActivity.class));
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return Musics.size();
        }

        class DrumPadMusicViewHolder extends RecyclerView.ViewHolder
        {
            ImageView MusicIcon;

            TextView MusicName;

            View v;

            DrumPadMusicViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                MusicIcon = itemView.findViewById(R.id.music_icon);

                MusicName = itemView.findViewById(R.id.music_name);
            }
        }
    }

}




