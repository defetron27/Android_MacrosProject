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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity
{
    private static final String TAG = AllUsersActivity.class.getSimpleName();

    private CollectionReference usersMobileNumberReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private String onlineUserId;

    private ArrayList<String> usersKey = new ArrayList<>();
    private ArrayList<String> mobileStorageContacts = new ArrayList<>();

    private PermissionUtil permissionUtil;

    private static final int READ_CONTACTS_PERMISSION = 190;
    private static final int REQUEST_CONTACTS_PERMISSION = 191;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        usersMobileNumberReference = FirebaseFirestore.getInstance().collection("Users");

        firebaseAuth = FirebaseAuth.getInstance();

        onlineUserId = firebaseAuth.getCurrentUser().getUid();

        currentUser = firebaseAuth.getCurrentUser();

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
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (number != null)
                    {
                        mobileStorageContacts.add(number);
                    }
                }
            }
            if (cursor != null)
            {
                cursor.close();
            }

            RecyclerView myChatsList = findViewById(R.id.all_users_list_view);
            myChatsList.setLayoutManager(new LinearLayoutManager(AllUsersActivity.this, LinearLayoutManager.VERTICAL, false));
            myChatsList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).color(Color.BLACK).build());
            final AllUsersAdapter allUsersAdapter = new AllUsersAdapter(this, usersKey);
            myChatsList.setAdapter(allUsersAdapter);

            usersMobileNumberReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        usersKey.clear();
                        for (QueryDocumentSnapshot document : task.getResult())
                        {
                            if (!document.getId().equals(onlineUserId))
                            {
                                String mobileNumberWithPlus = document.getString("user_mobile_number_with_plus");
                                String mobileNumberWithOutPlus = document.getString("user_mobile_number_with_out_plus");
                                String userId = document.getString("user_unique_id");

                                for (String number : mobileStorageContacts)
                                {
                                    if (number.equals(mobileNumberWithPlus) || number.equals(mobileNumberWithOutPlus))
                                    {
                                        usersKey.add(userId);
                                        Toast.makeText(AllUsersActivity.this, "equal number " + number, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        allUsersAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Crashlytics.log(Log.WARN,TAG,task.getException().toString());
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(AllUsersActivity.this, "Error occurred while database " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting documents " + e.getMessage());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }

    private class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>
    {
        Context context;
        ArrayList<String> usersKeys;

        AllUsersAdapter(Context context, ArrayList<String> usersKeys) {
            this.context = context;
            this.usersKeys = usersKeys;
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
            final String singleUserKey = usersKeys.get(position);

            holder.friendName.setVisibility(View.VISIBLE);
            holder.aboutFriend.setVisibility(View.VISIBLE);
            holder.friendProfileImage.setVisibility(View.VISIBLE);

            usersMobileNumberReference.document(singleUserKey).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(AllUsersActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        String userName = documentSnapshot.getString("user_name");
                        String thumbImg = documentSnapshot.getString("user_thumb_img");
                        final String visit_user_unique_id = documentSnapshot.getString("user_unique_id");

                        holder.friendName.setText(userName);
                        holder.setUser_thumb_img(AllUsersActivity.this,thumbImg);

                        if (documentSnapshot.contains("about_user"))
                        {
                            String userInviteStatus = documentSnapshot.getString("about_user");
                            holder.aboutFriend.setText(userInviteStatus);
                        }

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
                }
            });
        }

        @Override
        public int getItemCount() {
            return usersKeys.size();
        }

        class AllUsersViewHolder extends RecyclerView.ViewHolder
        {
            private TextView friendName,aboutFriend;
            private CircleImageView friendProfileImage;

            View v;

            AllUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;
                friendName = itemView.findViewById(R.id.all_users_invite_profile_name);
                aboutFriend = itemView.findViewById(R.id.all_users_invite_profile_about);
                friendProfileImage = itemView.findViewById(R.id.all_users_invite_profile_img);
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
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            FirebaseFirestore.getInstance().collection("Users").document(onlineUserId).update("online","true");
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(currentUser != null)
        {
            FirebaseFirestore.getInstance().collection("Users").document(onlineUserId).update("online", FieldValue.serverTimestamp());
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