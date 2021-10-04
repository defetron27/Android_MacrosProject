package com.deffe.macros.grindersouls;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference UsersDatabaseReference;

    MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user,parent,false);

        firebaseAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {
        final String online_user_id = firebaseAuth.getCurrentUser().getUid();

        final int count = 0;

        Messages messages = userMessagesList.get(position);

        final String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();


        UsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String userThumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                if (fromUserId.equals(online_user_id))
                {
                    holder.userProfileImg.setVisibility(View.GONE);
                }
                else
                {
                    holder.userProfileImg.setVisibility(View.VISIBLE);
                    Picasso.with(holder.userProfileImg.getContext()).load(userThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim).into(holder.userProfileImg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        if (fromMessageType.equals("text"))
        {
            if (fromUserId.equals(online_user_id))
            {
                holder.messageReceiverText.setVisibility(View.VISIBLE);

                holder.messageReceiverText.setTextColor(Color.BLACK);

                holder.messageReceiverText.setText(messages.getMessage());
            }
            else
            {
                holder.messageSenderText.setVisibility(View.VISIBLE);

                holder.messageSenderText.setTextColor(Color.WHITE);

                holder.messageSenderText.setText(messages.getMessage());
            }
        }
        else
        {
            if (fromUserId.equals(online_user_id))
            {
                holder.messageReceiverImageview.setVisibility(View.VISIBLE);

                Picasso.with(holder.userProfileImg.getContext()).load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.messageSenderImageView);
            }
            else
            {
                holder.messageSenderImageView.setVisibility(View.VISIBLE);

                Picasso.with(holder.userProfileImg.getContext()).load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.messageReceiverImageview);
            }

        }
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        TextView messageSenderText,messageReceiverText;

        CircleImageView userProfileImg;

        ImageView messageSenderImageView,messageReceiverImageview;

        MessageViewHolder(View view)
        {
            super(view);

            messageSenderText = view.findViewById(R.id.message_sender_text);

            messageReceiverText = view.findViewById(R.id.message_receiver_text);

            userProfileImg = view.findViewById(R.id.message_sender_profile_img);

            messageSenderImageView = view.findViewById(R.id.message_sender_image_view);

            messageReceiverImageview = view.findViewById(R.id.message_receiver_image_view);

        }
    }
}