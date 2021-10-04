package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.SharedPreferences;

public class PermissionUtil
{
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PermissionUtil(Context context)
    {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.permission_preference),Context.MODE_PRIVATE);
    }

    public void updatePermissionPreference(String permission)
    {
        editor = sharedPreferences.edit();

        switch (permission)
        {
            case "camera":
                editor.putBoolean(context.getString(R.string.permission_camera),true);
                editor.apply();
                break;
            case "storage":
                editor.putBoolean(context.getString(R.string.permission_storage),true);
                editor.apply();
                break;
            case "contacts":
                editor.putBoolean(context.getString(R.string.permission_contacts),true);
                editor.apply();
                break;
        }
    }

    public boolean checkPermissionPreference(String permission)
    {
       boolean isShown = false;

        switch (permission)
        {
            case "camera":
                isShown = sharedPreferences.getBoolean(context.getString(R.string.permission_camera),false);
                break;
            case "storage":
                isShown = sharedPreferences.getBoolean(context.getString(R.string.permission_storage),false);
                break;
            case "contacts":
                isShown = sharedPreferences.getBoolean(context.getString(R.string.permission_contacts),false);
                break;
        }
        return isShown;
    }
}
