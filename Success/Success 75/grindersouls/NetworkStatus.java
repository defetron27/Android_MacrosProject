package com.deffe.macros.grindersouls;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatus
{
    Context context;

    NetworkStatus(Context context)
    {
        this.context = context;
    }

    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = null;

        if (connectivityManager != null)
        {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.isConnected();
    }
}
