package com.deffe.macros.soulsspot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditGroupNameActivity extends AppCompatActivity
{
    private EditText EditGroupName;

    private Button CancelEditGroupButton;

    private Button DoneEditGroupButton;

    private String IncomingGroupName;

    private String AdminKey;

    private String GroupPosition;

    private String AdminName;

    private String CreatedGroupDate;

    private String GroupThumbImage;

    private DatabaseReference EditGroupNameReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_name);

        IncomingGroupName = getIntent().getExtras().getString("group_name");

        AdminKey = getIntent().getExtras().getString("admin_key");

        GroupPosition = getIntent().getExtras().getString("admin_group_key");

        GroupThumbImage = getIntent().getExtras().getString("group_thumb_image");

        AdminName = getIntent().getExtras().getString("admin_name");

        CreatedGroupDate = getIntent().getExtras().getString("date");

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

        EditGroupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        EditGroupNameReference.keepSynced(true);

        EditGroupName = findViewById(R.id.edit_group_name);

        CancelEditGroupButton = findViewById(R.id.cancel_edit_group_name_button);

        DoneEditGroupButton = findViewById(R.id.done_edit_group_name_button);

        EditGroupName.setText(IncomingGroupName);

        DoneEditGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String newName = EditGroupName.getText().toString();

                EditGroupNameReference.child(AdminKey).child(GroupPosition).child("new_group_name").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>()
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
                });


            }
        });

        CancelEditGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });
    }
}
