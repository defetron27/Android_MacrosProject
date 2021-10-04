package com.deffe.macros.grindersouls;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsViewHolder extends ChildViewHolder
{
    private TextView commentTextView,commentedUserName;
    private View view;
    private final static String TAG = CommentsViewHolder.class.getSimpleName();

    CommentsViewHolder(View itemView)
    {
        super(itemView);

        view = itemView;

        commentTextView = itemView.findViewById(R.id.comment_text_view);
        commentedUserName = itemView.findViewById(R.id.commented_user_name);
    }

    public void bind(Comments comments)
    {
        commentTextView.setText(comments.getComment());

        FirebaseFirestore.getInstance().collection("Users").document(comments.getComment_key()).addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(view.getContext(), "Error while getting documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }

                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    String name = documentSnapshot.getString("user_name");
                    String thumbImage = documentSnapshot.getString("user_thumb_img");

                    commentedUserName.setText(name);
                    setPostedUserImage(view.getContext(),thumbImage);
                }
            }
        });

    }

    void setPostedUserImage(final Context context, final String user_thumb_img) {

        final CircleImageView thumb_img = view.findViewById(R.id.commented_user_image);

        Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE)
                .into(thumb_img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                    }
                });
    }
}
