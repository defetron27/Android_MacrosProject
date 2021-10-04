package com.deffe.macros.animations;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.BaseAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllUsersActivity extends EuclidActivity
{


    String online_user_id;

    DatabaseReference UsersReference;

    FirebaseAuth firebaseAuth;

    private FirebaseUser currentUserMobileNumber;

    private DatabaseReference databaseReference;

    ArrayList<String> UserKey = new ArrayList<>();

    String[] namearray;
    String[] phonearray;

    String namecsv = "";
    String phonecsv = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersReference.keepSynced(true);


        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

        while (cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (name != null)
            {
                namecsv += name + ",";
                phonecsv += number + ",";
            }
        }
        cursor.close();

        namearray = namecsv.split(",");
        phonearray = phonecsv.split(",");

    }

    @Override
    protected BaseAdapter getAdapter()
    {
        return null;
    }

    private void usersData()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);


        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UserKey.clear();
                for (DataSnapshot single: dataSnapshot.getChildren())
                {
                    if (!single.getKey().equals(online_user_id))
                    {
                        UserKey.add(single.getKey());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }

}
