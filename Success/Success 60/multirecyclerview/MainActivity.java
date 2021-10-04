package com.deffe.max.multirecyclerview;

import android.content.res.AssetFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private ArrayList<Model> List = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List.add(new Model(Model.TEXT_TYPE,"Hello, hi how are your this text view",0));

        List.add(new Model(Model.IMAGE_TYPE,"Hello, hi this image view",R.drawable.steve));

        List.add(new Model(Model.VIDEO_TYPE,"Hello, hi this video view", R.raw.flute));

        MultiRecyclerViewAdapter adapter = new MultiRecyclerViewAdapter(List,MainActivity.this);

        RecyclerView recyclerView = findViewById(R.id.multi_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }

}
