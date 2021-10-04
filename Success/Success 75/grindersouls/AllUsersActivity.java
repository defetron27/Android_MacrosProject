package com.deffe.macros.grindersouls;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity
{
    private String online_user_id;

    DatabaseReference UsersReference;

    FirebaseAuth firebaseAuth;

    private DatabaseReference databaseReference;

    ArrayList<String> UserKey = new ArrayList<>();

    String[] namearray;
    String[] phonearray;

    String namecsv = "";
    String phonecsv = "";

    AllUsersAdapter allUsersAdapter ;

    private PermissionUtil permissionUtil;

    private static final int READ_CONTACTS_PERMISSION = 190;

    private static final int REQUEST_CONTACTS_PERMISSION = 190;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersReference.keepSynced(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);

        Toolbar allFriendsToolbar = findViewById(R.id.all_friends_tool_bar);
        setSupportActionBar(allFriendsToolbar);
        getSupportActionBar().setTitle("All Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        permissionUtil = new PermissionUtil(this);

        readContact();
    }

    private int checkPermission(int permission)
    {
        int status = PackageManager.PERMISSION_DENIED;

        switch (permission)
        {
            case READ_CONTACTS_PERMISSION:
                status = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS);
                break;
        }
        return status;
    }

    private void requestPermission(int permission)
    {
        switch (permission)
        {
            case READ_CONTACTS_PERMISSION:
                ActivityCompat.requestPermissions(AllUsersActivity.this,new String[]{Manifest.permission.READ_CONTACTS},REQUEST_CONTACTS_PERMISSION);
                break;
        }
    }

    private void showPermissionExplanation(final int permission)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (permission == READ_CONTACTS_PERMISSION)
        {
            builder.setMessage("This app need to access your contacts..Please allow");
            builder.setTitle("Contacts Permission Needed..");
        }

        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (permission == READ_CONTACTS_PERMISSION)
                {
                    requestPermission(READ_CONTACTS_PERMISSION);
                }
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void readContact()
    {
        if (checkPermission(READ_CONTACTS_PERMISSION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AllUsersActivity.this,Manifest.permission.READ_CONTACTS))
            {
                showPermissionExplanation(READ_CONTACTS_PERMISSION);
            }
            else if (!permissionUtil.checkPermissionPreference("contacts"))
            {
                requestPermission(READ_CONTACTS_PERMISSION);
                permissionUtil.updatePermissionPreference("contacts");
            }
            else
            {
                Toast.makeText(this, "Please allow Contacts permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }
        else
        {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            if (cursor != null)
            {
                while (cursor.moveToNext())
                {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (name != null)
                    {
                        namecsv += (name + ",");
                        phonecsv += (number + ",");
                    }
                }
            }
            if (cursor != null)
            {
                cursor.close();
            }

            namearray = namecsv.split(",");
            phonearray = phonecsv.split(",");

            RecyclerView myChatsList = findViewById(R.id.all_users_list_view);
            myChatsList.setLayoutManager(new LinearLayoutManager(AllUsersActivity.this, LinearLayoutManager.VERTICAL, false));
            myChatsList
                    .addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                            .color(Color.BLACK)
                            .build());
            allUsersAdapter = new AllUsersAdapter(this);
            myChatsList.setAdapter(allUsersAdapter);

            databaseReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    UserKey.clear();
                    for (DataSnapshot single : dataSnapshot.getChildren())
                    {
                        final String userMobileNumberWithPlus = (String) single.child("user_mobile_number_with_plus").getValue();
                        final String userMobileNumberWithOutPlus = (String) single.child("user_mobile_number_with_out_plus").getValue();

                        if (!single.getKey().equals(online_user_id))
                        {
                            for (String aPhonearray : phonearray)
                            {
                                if (aPhonearray.equals(userMobileNumberWithPlus) || aPhonearray.equals(userMobileNumberWithOutPlus))
                                {
                                    UserKey.add(single.getKey());
                                    break;
                                }
                            }

                        }
                    }
                    allUsersAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                }
            });
        }
    }

    private class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>
    {
        Context context;

        AllUsersAdapter(AllUsersActivity activity)
        {
            this.context = activity;
        }

        @NonNull
        @Override
        public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_items, parent, false);

            return new AllUsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllUsersViewHolder holder, int position)
        {
            final String singleUserKey = UserKey.get(position);

            UsersReference.child(singleUserKey).addValueEventListener(new ValueEventListener()
            {

                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
                {
                    final String userName = (String) dataSnapshot.child("user_name").getValue();

                    final String userInviteStatus = (String) dataSnapshot.child("user_invite_profile_status").getValue();
                    String thumbImg = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String visit_user_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    holder.name.setText(userName);
                    holder.status.setText(userInviteStatus);
                    holder.setUser_thumb_img(AllUsersActivity.this,thumbImg);

                    holder.v.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent viewProfileIntent= new Intent(AllUsersActivity.this,ViewUserProfileActivity.class);
                            viewProfileIntent.putExtra("visit_user_unique_id",visit_user_unique_id);
                            startActivity(viewProfileIntent);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                }
            });
        }

        @Override
        public int getItemCount() {
            return UserKey.size();
        }

        class AllUsersViewHolder extends RecyclerView.ViewHolder
        {
            private TextView name,status;
            View v;

            AllUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;
                name = itemView.findViewById(R.id.all_users_invite_profile_name);
                status = itemView.findViewById(R.id.all_users_invite_profile_status);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.all_users_invite_profile_img);

                Picasso.with(c).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(thumb_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(c).load(user_thumb_img).placeholder(R.drawable.register_user).into(thumb_img);
                            }
                        });
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.all_friends_search_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_add_contacts)
        {
            Intent contactsIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
            contactsIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            startActivity(contactsIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}