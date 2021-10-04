package com.deffe.macros.grindersouls;



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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

public class ChatsFragment extends Fragment
{

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


        Log.i("MainActivity","Chat onResume");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String online_user_id = firebaseAuth.getCurrentUser().getUid();

        ChatUserFriendReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatUserFriendReference.keepSynced(true);

        ChatFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        ChatFriendsReference.keepSynced(true);

        RecyclerView chatUserFriendRecyclerView = ChatUserFriendView.findViewById(R.id.chat_user_friends);
        chatUserFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        chatUserFriendRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        allChatUserFriendsAdapter = new AllChatUserFriendsAdapter(ChatUserFriendsKey,getActivity());
        chatUserFriendRecyclerView.setAdapter(allChatUserFriendsAdapter);

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

        return ChatUserFriendView;
    }

    private class AllChatUserFriendsAdapter extends RecyclerView.Adapter<AllChatUserFriendsAdapter.ChatUserFriendsViewHolder>
    {
        ArrayList<String> ChatUserFriendKey;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_fragment_recycler_view_items,parent,false);

            return new ChatUserFriendsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ChatUserFriendsViewHolder holder, int position)
        {
            final String chatUserFriendId = ChatUserFriendKey.get(position);

            ChatUserFriendReference.child(chatUserFriendId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot)
                {
                    final String friendName = (String) dataSnapshot.child("user_name").getValue();
                    final String friendThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();

                    if(dataSnapshot.hasChild("online"))
                    {
                        String online_status =  dataSnapshot.child("online").getValue().toString();

                        if (online_status.equals("true"))
                        {
                            holder.setUserOnline(online_status);
                            holder.userFriendLastSeen.setText("online");
                        }
                        else
                        {
                            holder.setUserOnline(online_status);

                            long last_seen = Long.parseLong(online_status);

                            String lastSeenDisplayTime = LastSeenTime.getTimeAgo(last_seen, getContext());

                            holder.userFriendLastSeen.setText(lastSeenDisplayTime);
                        }
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
                                Bundle bundle = new Bundle();

                                ArrayList<String> SingleChat = new ArrayList<>();
                                SingleChat.add(chatUserFriendId);

                                bundle.putStringArrayList("ids",SingleChat);

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.setType("single");
                                chatIntent.setAction(Intent.ACTION_VIEW);
                                chatIntent.putExtras(bundle);
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
                                                Bundle bundle = new Bundle();

                                                ArrayList<String> SingleChat = new ArrayList<>();
                                                SingleChat.add(chatUserFriendId);

                                                bundle.putStringArrayList("ids",SingleChat);

                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.setType("single");
                                                chatIntent.putExtras(bundle);
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

        @Override
        public int getItemCount()
        {
            return ChatUserFriendKey.size();
        }

        class ChatUserFriendsViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,userFriendLastSeen;

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
