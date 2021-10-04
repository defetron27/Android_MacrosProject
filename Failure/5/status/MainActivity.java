package com.deffe.macros.status;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = MainActivity.class.getSimpleName();



    private DatabaseReference UserDatabaseReference,GroupRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;


    private final ArrayList<BaseVideoItem> mList = new ArrayList<>();


    private final ListItemsVisibilityCalculator mVideoVisibilityCalculator =
            new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mList);

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private ItemsPositionGetter mItemsPositionGetter;

    private final VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();




        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();


                storageReference = FirebaseStorage.getInstance().getReference().child(currentUser.getUid() ).child("tos.mp4");

                File videoPath = new File(Environment.getExternalStorageDirectory(), "SoulsSpot");

                if (!videoPath.exists()) {
                    videoPath.mkdirs();
                }


                final File localeFile = new File(videoPath, "tos.mp4");

                storageReference.getFile(localeFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                    {
                        Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();

                        try
                        {
                            Toast.makeText(MainActivity.this, "Location Pl "+localeFile, Toast.LENGTH_SHORT).show();
                            mList.add(ItemFactory.createItemFromUri(mVideoPlayerManager, Uri.fromFile(localeFile),MainActivity.this));
                        }
                        catch (IOException e)
                        {
                            Toast.makeText(MainActivity.this, "Please "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Download Failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




        }



        VideoRecyclerViewAdapter videoRecyclerViewAdapter = new VideoRecyclerViewAdapter(mVideoPlayerManager, MainActivity.this, mList);

        mRecyclerView.setAdapter(videoRecyclerViewAdapter);
        mItemsPositionGetter = new RecyclerViewItemPositionGetter(mLayoutManager, mRecyclerView);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mVideoPlayerManager.resetMediaPlayer();
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null)
        {
            LogOutUser();
        }
    }


    private void LogOutUser()
    {
        Intent startPageIntent = new Intent(MainActivity.this,LoginActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

}
