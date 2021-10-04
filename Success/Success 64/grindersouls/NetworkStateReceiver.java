package com.deffe.macros.grindersouls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver
{

    public static NetworkStateReceiverListener networkStateReceiverListener;

    public NetworkStateReceiver()
    {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);


        NetworkInfo activeNetwork = null;

        if (connectivityManager != null)
        {
            activeNetwork = connectivityManager.getActiveNetworkInfo();
        }
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (networkStateReceiverListener != null)
        {
            networkStateReceiverListener.onNetworkConnectionChanged(isConnected);
        }

    }

    public static boolean isConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) GrinderSoulsApp_Offline.getInstance().getApplicationContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public interface NetworkStateReceiverListener
    {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
