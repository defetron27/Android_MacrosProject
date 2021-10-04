package com.deffe.macros.grindersouls;

import android.content.Intent;
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

public class MindFreeActivity extends AppCompatActivity
{
    ArrayList<String> Games;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_free);

        Games = new ArrayList<>();

        Games.add("DrumPad");
        Games.add("JigSaw Puzzle");

        MindFreeGamesAdapter adapter = new MindFreeGamesAdapter();

        RecyclerView mindFreeRecyclerView = findViewById(R.id.mind_free_recycler_view);
        mindFreeRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mindFreeRecyclerView.setAdapter(adapter);
    }

    public class MindFreeGamesAdapter extends RecyclerView.Adapter<MindFreeGamesAdapter.MindFreeViewHolder>
    {
        @NonNull
        @Override
        public MindFreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mind_free_layout_items,parent,false);

            return new MindFreeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MindFreeViewHolder holder, int position)
        {
            final String game = Games.get(position);

            if (game.equals("DrumPad"))
            {
                holder.GameIcon.setImageDrawable(getResources().getDrawable(R.drawable.jigsaw));
            }
            else if (game.equals("JigSaw Puzzle"))
            {
                holder.GameIcon.setImageDrawable(getResources().getDrawable(R.drawable.drumpad));
            }

            holder.GameName.setText(game);

            holder.v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (game.equals("DrumPad"))
                    {
                        startActivity(new Intent(MindFreeActivity.this,DrumPadActivity.class));
                    }
                    else if (game.equals("JigSaw Puzzle"))
                    {
                        startActivity(new Intent(MindFreeActivity.this,JigSawPuzzleMainActivity.class));
                    }
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return Games.size();
        }

        class MindFreeViewHolder extends RecyclerView.ViewHolder
        {
            ImageView GameIcon;

            TextView GameName;

            View v;

            MindFreeViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                GameIcon = itemView.findViewById(R.id.game_icon);

                GameName = itemView.findViewById(R.id.game_name);
            }
        }
    }
}
