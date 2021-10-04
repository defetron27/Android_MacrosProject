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

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment
{
    private final static String TAG = ChatsFragment.class.getSimpleName();

    private ArrayList<String> ChatUserFriendsKey = new ArrayList<>();

    public ChatsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View chatUserFriendView = inflater.inflate(R.layout.fragment_chats, container, false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String online_user_id = firebaseAuth.getCurrentUser().getUid();

        CollectionReference chatFriendsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(online_user_id).collection("Friends");

        final RecyclerView chatUserFriendRecyclerView = chatUserFriendView.findViewById(R.id.chat_user_friends);
        chatUserFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        chatUserFriendRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());

        chatFriendsReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
                if (queryDocumentSnapshots != null)
                {
                    ChatUserFriendsKey.clear();

                    for (QueryDocumentSnapshot single: queryDocumentSnapshots)
                    {
                        ChatUserFriendsKey.add(single.getId());
                    }

                    AllChatUserFriendsAdapter allChatUserFriendsAdapter = new AllChatUserFriendsAdapter(ChatUserFriendsKey,getContext());
                    chatUserFriendRecyclerView.setAdapter(allChatUserFriendsAdapter);
                }
            }
        });

        return chatUserFriendView;
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

            final CollectionReference chatUserFriendReference = FirebaseFirestore.getInstance().collection("Users");

            holder.userName.setVisibility(View.VISIBLE);
            holder.userImage.setVisibility(View.VISIBLE);

            chatUserFriendReference.document(chatUserFriendId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@javax.annotation.Nullable final DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        final String friendName = documentSnapshot.getString("user_name");
                        final String friendThumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.userName.setText(friendName);
                        holder.setUser_thumb_img(getContext(),friendThumbImage);

                        if (documentSnapshot.contains("online"))
                        {
                            final String online_status = documentSnapshot.get("online").toString();

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
                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (documentSnapshot.contains("online"))
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
                                    Map<String, Object> online = new HashMap<>();
                                    online.put("online","true");

                                    chatUserFriendReference.document(chatUserFriendId).set(online)
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
                                            }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        }
                                    });
                                }
                            }
                        });
                    }
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
            CircleImageView userImage,online;

            ChatUserFriendsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = v.findViewById(R.id.chat_user_name);
                userFriendLastSeen = v.findViewById(R.id.chat_user_last_seen_time);
                userImage = v.findViewById(R.id.chat_user_image);
                online = v.findViewById(R.id.chat_user_online_status);

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
                    userFriendLastSeen.setVisibility(View.GONE);
                }
                else
                {
                    onlineStatusView.setVisibility(View.GONE);
                    userFriendLastSeen.setVisibility(View.VISIBLE);
                }
            }

        }
    }
}
