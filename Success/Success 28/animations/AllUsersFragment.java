package com.deffe.macros.animations;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class AllUsersFragment extends Fragment
{


    RecyclerView myChatsList;

    String online_user_id;

    DatabaseReference UsersReference;

    FirebaseAuth firebaseAuth;

    private FirebaseUser currentUserMobileNumber;

    private DatabaseReference databaseReference;

    AllUsersAdapter allUsersAdapter ;

    ArrayList<String> UserKey = new ArrayList<>();


    View myMainView;

    String[] namearray;
    String[] phonearray;

    String namecsv = "";
    String phonecsv = "";


    ArrayList<String> UsersMobileNumbersInFirebase = new ArrayList<>();

    String countryCode = "";

    public AllUsersFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        myMainView = inflater.inflate(R.layout.fragment_all_users, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersReference.keepSynced(true);


        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

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


        return myMainView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.myMainView = view;



        usersInit();
        usersData();
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
                allUsersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }
    private void usersInit()
    {
        myChatsList = (RecyclerView) myMainView.findViewById(R.id.all_users_list);
        myChatsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        allUsersAdapter = new AllUsersAdapter(getActivity());
        myChatsList.setAdapter(allUsersAdapter);

    }

    public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>
    {
        Context context;

        AllUsersAdapter(FragmentActivity activity) {
            this.context = activity;
        }

        @NonNull
        @Override
        public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_list_item, parent, false);

            return new AllUsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllUsersViewHolder holder, int position)
        {
            final String singleUserKey = UserKey.get(position);

            UsersMobileNumbersInFirebase.clear();

            UsersReference.child(singleUserKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot)
                {
                    final String userName = (String) dataSnapshot.child("user_name").getValue();
                    final String userMobileNumber = (String) dataSnapshot.child("user_mobile_number").getValue();
                    String thumbImg = (String) dataSnapshot.child("user_thumb_img").getValue();

                    UsersMobileNumbersInFirebase.add(userMobileNumber);

                    countryCode = "+91"+userMobileNumber;

                    for (String aPhonearray : phonearray)
                    {
                        if (aPhonearray.equals(userMobileNumber) || aPhonearray.equals(countryCode))
                        {
                            holder.name.setText(userName);
                            holder.mobileNumber.setText(userMobileNumber);
                            holder.setUser_thumb_img(getContext(),thumbImg);
                        }
                    }

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
            private TextView mobileNumber,name;
            View v;

            AllUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;
                name = (TextView) itemView.findViewById(R.id.user_name);
                mobileNumber = (TextView) itemView.findViewById(R.id.user_mobile_number);
            }

            public void setUser_thumb_img(final Context c,final String user_thumb_img)
            {
                final CircleImageView thumb_img = (CircleImageView) v.findViewById(R.id.all_users_profile_img);

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


    public ArrayList<String> getUsersMobileNumbersInFirebase()
    {
        return UsersMobileNumbersInFirebase;
    }

    public void setUsersMobileNumbersInFirebase(ArrayList<String> usersMobileNumbersInFirebase)
    {
        UsersMobileNumbersInFirebase = usersMobileNumbersInFirebase;
    }

}
