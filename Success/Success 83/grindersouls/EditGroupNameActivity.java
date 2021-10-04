package com.deffe.macros.grindersouls;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditGroupNameActivity extends BaseThemedActivity
{
    private EditText EditGroupName;

    private String GroupKey;

    private CollectionReference EditGroupNameReference;

    private final static String TAG = EditGroupNameActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_name);

        String incomingGroupName = getIntent().getExtras().getString("group_name");

        GroupKey = getIntent().getExtras().getString("group_key");

        Toolbar EditGroupNameActivityToolBar = findViewById(R.id.edit_group_name_toolbar);
        setSupportActionBar(EditGroupNameActivityToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditGroupNameActivityToolBar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        EditGroupNameReference = FirebaseFirestore.getInstance().collection("Groups");

        EditGroupName = findViewById(R.id.edit_group_name);

        Button cancelEditGroupButton = findViewById(R.id.cancel_edit_group_name_button);

        Button doneEditGroupButton = findViewById(R.id.done_edit_group_name_button);

        EditGroupName.setText(incomingGroupName);

        doneEditGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String newName = EditGroupName.getText().toString();

                Map<String,Object> groupName = new HashMap<>();
                groupName.put("group_name",newName);

                EditGroupNameReference.document(GroupKey).update(groupName).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(EditGroupNameActivity.this, "Group name updated successfully", Toast.LENGTH_SHORT).show();

                            Intent backToViewGroupIntent = new Intent();
                            backToViewGroupIntent.putExtra("group_name",newName);
                            setResult(RESULT_OK,backToViewGroupIntent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(EditGroupNameActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });
            }
        });

        cancelEditGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });
    }
}
