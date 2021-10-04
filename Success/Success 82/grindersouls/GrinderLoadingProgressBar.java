package com.deffe.macros.grindersouls;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

public class GrinderLoadingProgressBar
{
    private ProgressDialog progressDialog;

    GrinderLoadingProgressBar()
    {
    }

    public void showLoadingBar(Context context, String message)
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideLoadingBar()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }
}
