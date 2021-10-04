package com.deffe.macros.soulsspot;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alexfu.countdownview.CountDownView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_SHORT;

public class AllFriendsActivity extends AppCompatActivity
{
    private Toolbar allFriendsToolbar;

    RecyclerView myChatsList;

    String online_user_id;

    DatabaseReference UsersReference;

    FirebaseAuth firebaseAuth;

    private DatabaseReference databaseReference;

    ArrayList<String> UserKey = new ArrayList<>();

    String[] namearray;
    String[] phonearray;

    String namecsv = "";
    String phonecsv = "";

    AllUsersAdapter allUsersAdapter ;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_friends);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersReference.keepSynced(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);

        allFriendsToolbar = findViewById(R.id.all_friends_tool_bar);
        setSupportActionBar(allFriendsToolbar);
        getSupportActionBar().setTitle("All Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (name != null) {
                    namecsv += (name + ",");
                    phonecsv += (number + ",");
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        namearray = namecsv.split(",");
        phonearray = phonecsv.split(",");

        myChatsList = findViewById(R.id.all_users_list_view);
        myChatsList.setLayoutManager(new LinearLayoutManager(AllFriendsActivity.this, LinearLayoutManager.VERTICAL, false));
        myChatsList
                .addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                        .color(Color.BLACK)
                        .build());
        allUsersAdapter = new AllUsersAdapter(this);
        myChatsList.setAdapter(allUsersAdapter);

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
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
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });


    }

    private class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>
    {
        Context context;



        AllUsersAdapter(AllFriendsActivity activity) {
            this.context = activity;
        }

        @NonNull
        @Override
        public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_items, parent, false);

            return new AllUsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllUsersViewHolder holder, int position)
        {
            final String singleUserKey = UserKey.get(position);
            final ArrayList<String> FinalMembers = new ArrayList<>();



            UsersReference.child(singleUserKey).addValueEventListener(new ValueEventListener()
            {

                @Override
                public void onDataChange(final DataSnapshot dataSnapshot)
                {
                    final String userName = (String) dataSnapshot.child("user_name").getValue();

                    final String userInviteStatus = (String) dataSnapshot.child("user_invite_profile_status").getValue();
                    String thumbImg = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String visit_user_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    holder.name.setText(userName);
                    holder.status.setText(userInviteStatus);
                    holder.setUser_thumb_img(AllFriendsActivity.this,thumbImg);

                    holder.v.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent viewProfileIntent= new Intent(AllFriendsActivity.this,ViewProfileActivity.class);
                            viewProfileIntent.putExtra("visit_user_unique_id",visit_user_unique_id);
                            startActivity(viewProfileIntent);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
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
            private TextView mobileNumber,name,status;
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

                Picasso.with(c).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.register_user)
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
