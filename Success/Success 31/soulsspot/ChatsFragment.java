package com.deffe.macros.soulsspot;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment implements ISearch
{
    private static final String ARG_SEARCHTERM = "search_term";

    private String mSearchTerm = null;

    private IFragmentListener mIFragmentListener = null;

    private View ChatUserFriendView;

    private DatabaseReference ChatUserFriendReference,ChatFriendsReference;

    private ArrayList<String> ChatUserFriendsKey = new ArrayList<>();

    private AllChatUserFriendsAdapter allChatUserFriendsAdapter;

    public ChatsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ChatUserFriendView = inflater.inflate(R.layout.fragment_chats, container, false);

        if(getArguments()!=null)
        {
            mSearchTerm = (String) getArguments().get(ARG_SEARCHTERM);
        }

        return ChatUserFriendView;
    }

    @Override
    public void onViewCreated(@NonNull View mainView, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(mainView, savedInstanceState);

        this.ChatUserFriendView = mainView;

        init();

        loadData();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(null!=mSearchTerm)
        {
            onTextQuery(mSearchTerm);
        }
    }

    private void init()
    {
        RecyclerView chatUserFriendRecyclerView = ChatUserFriendView.findViewById(R.id.chat_user_friends);
        chatUserFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        chatUserFriendRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        allChatUserFriendsAdapter = new AllChatUserFriendsAdapter(ChatUserFriendsKey,getActivity());
        chatUserFriendRecyclerView.setAdapter(allChatUserFriendsAdapter);
    }

    private void loadData()
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String online_user_id = firebaseAuth.getCurrentUser().getUid();

        ChatUserFriendReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatUserFriendReference.keepSynced(true);

        ChatFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        ChatFriendsReference.keepSynced(true);

        ChatFriendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ChatUserFriendsKey.clear();

                for (DataSnapshot single: dataSnapshot.getChildren())
                {

                    ChatUserFriendsKey.add(single.getKey());
                }
                allChatUserFriendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }

    @Override
    public void onTextQuery(String text)
    {
        Query searchPeopleAndFriends = ChatFriendsReference.orderByChild("user_name")
                .startAt(text).endAt(text + "\uf8ff");

        searchPeopleAndFriends.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ChatUserFriendsKey.clear();

                for (DataSnapshot single: dataSnapshot.getChildren())
                {
                    ChatUserFriendsKey.add(single.getKey());
                }
                allChatUserFriendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    public static ChatsFragment newInstance(String searchTerm)
    {
        ChatsFragment fragment = new ChatsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SEARCHTERM, searchTerm);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mIFragmentListener = (IFragmentListener) context;
        mIFragmentListener.addiSearch(ChatsFragment.this);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if(null!=mIFragmentListener)
        {
            mIFragmentListener.removeISearch(ChatsFragment.this);
        }
    }



    private class AllChatUserFriendsAdapter extends RecyclerView.Adapter<AllChatUserFriendsAdapter.ChatUserFriendsViewHolder>
    {
        ArrayList<String> ChatUserFriendKey = new ArrayList<>();
        Context context;

        AllChatUserFriendsAdapter(ArrayList<String> ChatUserFriendKey, Context context)
        {
            this.ChatUserFriendKey = ChatUserFriendKey;
            this.context = context;
        }

        @NonNull
        @Override
        public ChatUserFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_users_recycler_view_items,parent,false);

            return new ChatUserFriendsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ChatUserFriendsViewHolder holder, int position)
        {
            final String chatUserFriendId = ChatUserFriendKey.get(position);

            final int userKeySize = ChatUserFriendKey.size();

            if (userKeySize > 0)
            {

                ChatUserFriendReference.child(chatUserFriendId).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot)
                    {
                        final String friendName = (String) dataSnapshot.child("user_name").getValue();
                        final String friendThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();

                        if(dataSnapshot.hasChild("online"))
                        {
                            String online_status = dataSnapshot.child("online").getValue().toString();

                            holder.setUserOnline(online_status);

                            long last_seen = Long.parseLong(online_status);

                            String lastSeenDisplayTime = LastSeenTime.getTimeAgo(last_seen, getContext());

                            holder.userFriendLastSeen.setText(lastSeenDisplayTime);
                        }

                        holder.userName.setText(friendName);
                        holder.setUser_thumb_img(getContext(),friendThumbImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (dataSnapshot.child("online").exists())
                                {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",chatUserFriendId);
                                    chatIntent.putExtra("user_name",friendName);
                                    startActivity(chatIntent);
                                }
                                else
                                {
                                    ChatUserFriendReference.child(chatUserFriendId).child("online").setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id",chatUserFriendId);
                                                    chatIntent.putExtra("user_name",friendName);
                                                    startActivity(chatIntent);
                                                }
                                            });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
        }

        @Override
        public int getItemCount()
        {
            return ChatUserFriendKey.size();
        }

        class ChatUserFriendsViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,userFriendLastSeen;

            Button unFriendButton;

            ChatUserFriendsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = v.findViewById(R.id.chat_user_name);
                userFriendLastSeen = v.findViewById(R.id.chat_user_last_seen_time);

            }
            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.chat_user_image);

                Picasso.with(c).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(thumb_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(c).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }

            void setUserOnline(String online_status)
            {
                ImageView onlineStatusView = v.findViewById(R.id.chat_user_online_status);

                if(online_status.equals("true"))
                {
                    onlineStatusView.setVisibility(View.VISIBLE);
                    userFriendLastSeen.setVisibility(View.INVISIBLE);
                }
                else
                {
                    onlineStatusView.setVisibility(View.INVISIBLE);
                    userFriendLastSeen.setVisibility(View.VISIBLE);
                }
            }

        }
    }
}
