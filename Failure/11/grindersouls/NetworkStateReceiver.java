package com.deffe.macros.grindersouls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkStateReceiver extends BroadcastReceiver
{
    public static NetworkStateReceiverListener networkStateReceiverListener;

    public NetworkStateReceiver()
    {
        super();
    }

    private static NetworkInfo getNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;

        if (connectivityManager != null)
        {
            activeNetwork = connectivityManager.getActiveNetworkInfo();
        }

        return activeNetwork;
    }

    public static boolean isConnected(Context context)
    {
        NetworkInfo activeNetworkInfo = NetworkStateReceiver.getNetworkInfo(context);

        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

    public static boolean isConnectedWifi(Context context)
    {
        NetworkInfo activeNetworkInfo = NetworkStateReceiver.getNetworkInfo(context);

        return (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedMobile(Context context)
    {
        NetworkInfo activeNetworkInfo = NetworkStateReceiver.getNetworkInfo(context);

        return (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isConnectedFast(Context context)
    {
        NetworkInfo activeNetworkInfo = NetworkStateReceiver.getNetworkInfo(context);

        return (activeNetworkInfo != null && activeNetworkInfo.isConnected() && NetworkStateReceiver.isConnectionFast(activeNetworkInfo.getType(),activeNetworkInfo.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType)
    {
        if (type == ConnectivityManager.TYPE_WIFI)
        {
            return true;
        }
        else if (type == ConnectivityManager.TYPE_MOBILE)
        {
            switch (subType)
            {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return true; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false; // ~ 25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return true; // ~ 10+ Mbps
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        String type = intent.getType();

        boolean isConnected = isConnected(context);

        if (networkStateReceiverListener != null)
        {
            networkStateReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    public interface NetworkStateReceiverListener
    {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
