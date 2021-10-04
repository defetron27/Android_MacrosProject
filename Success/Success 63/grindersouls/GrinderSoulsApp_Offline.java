package com.deffe.macros.grindersouls;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class GrinderSoulsApp_Offline extends Application
{
    private static GrinderSoulsApp_Offline mInstance;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mInstance = this;

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }

    public static synchronized GrinderSoulsApp_Offline getInstance()
    {
        return mInstance;
    }

    public void setConnectionListener(NetworkStateReceiver.NetworkStateReceiverListener networkStateReceiverListener)
    {
        NetworkStateReceiver.networkStateReceiverListener = networkStateReceiverListener;
    }
}
