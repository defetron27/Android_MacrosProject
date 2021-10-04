package com.deffe.macros.animations;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.labo.kaji.fragmentanimations.CubeAnimation;


public class ChatsFragment extends Fragment
{

    private static final long DURATION = 500;


    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
    {
        return CubeAnimation.create(CubeAnimation.UP, enter, DURATION);
    }



    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

}
