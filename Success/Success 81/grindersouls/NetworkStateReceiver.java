package com.deffe.macros.grindersouls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver
{
    public static final String NETWORK_AVAILABLE_ACTION = "com.deffe.macros.grinderssouls";
    public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent networkStateIntent = new Intent(NETWORK_AVAILABLE_ACTION);
        networkStateIntent.putExtra(IS_NETWORK_AVAILABLE, isConnected(context));

        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);
    }

    public boolean isConnected(Context context)
    {
       try
       {
           if (context != null)
           {
               ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

               NetworkInfo networkInfo = null;

               if (connectivityManager != null)
               {
                   networkInfo = connectivityManager.getActiveNetworkInfo();
               }

               return networkInfo != null && networkInfo.isConnected();
           }
           return false;
       }
       catch (Exception e)
       {
           Log.e(NetworkStateReceiver.class.getName(),e.getMessage());
           return false;
       }
    }

}
