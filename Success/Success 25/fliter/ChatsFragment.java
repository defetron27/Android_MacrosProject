package com.deffe.macros.fliter;


import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment
{


    RecyclerView myChatsList;

    String online_user_id;

    DatabaseReference UsersReference;
    FirebaseAuth firebaseAuth;

    ChatsAdapter chatsAdapter;

    ArrayList<String> UserKey = new ArrayList<>();


    View myMainView;

    String[] namearray;
    String[] phonearray;

    String namecsv = "";
    String phonecsv = "";

    ArrayList<String> UsersMobileNumbersInFirebase = new ArrayList<>();

    String countryCode = "";

    public ChatsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        myMainView = inflater.inflate(R.layout.fragment_chats, container, false);

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.myMainView = view;

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        chatInit();
        chatData();
    }


    private void chatData()
    {
        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UserKey.clear();
                for (DataSnapshot single: dataSnapshot.getChildren())
                {
                    UserKey.add(single.getKey());
                }
                chatsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }
    private void chatInit()
    {
        myChatsList = (RecyclerView) myMainView.findViewById(R.id.chats_list);
        myChatsList.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        chatsAdapter = new ChatsAdapter(getActivity());
        myChatsList.setAdapter(chatsAdapter);

    }
    public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
        Context context;

        public ChatsAdapter(FragmentActivity activity) {
            this.context = activity;
        }

        @Override
        public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_list_item, parent, false);

            return new ChatsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ChatsViewHolder holder, int position)
        {
            final String singleUserKey = UserKey.get(position);

            UsersMobileNumbersInFirebase.clear();

            UsersReference.child(singleUserKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot)
                {
                    final String userMobileNumber = (String) dataSnapshot.child("user_mobile_number").getValue();

                    UsersMobileNumbersInFirebase.add(userMobileNumber);

                    countryCode = "+91"+userMobileNumber;

                    for (int i=0; i<phonearray.length;i++)
                    {
                        if (phonearray[i].equals(userMobileNumber) || phonearray[i].equals(countryCode))
                        {
                            holder.name.setText(userMobileNumber);
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

        public class ChatsViewHolder extends RecyclerView.ViewHolder
        {
            private TextView name;
            View v;

            public ChatsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;
                name = (TextView) itemView.findViewById(R.id.user_mobile_number);
            }
        }
    }




}
