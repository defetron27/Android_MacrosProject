package com.deffe.macros.status;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseReference database;

    private Uri videoUri;


    private File localeFile;

    private static final int REQUEST_CODE = 1;


    private final ArrayList<BaseVideoItem> mList = new ArrayList<>();

//    private final ArrayList<String> videoList = new ArrayList<>();


    private final ListItemsVisibilityCalculator mVideoVisibilityCalculator =
            new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mList) {
                @Override
                public void onScrollDirectionChanged(ScrollDirectionDetector.ScrollDirection scrollDirection) {

                }

                @Override
                protected void onStateFling(ItemsPositionGetter itemsPositionGetter) {

                }
            };

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;


    private DatabaseReference UserDatabaseReference,GroupRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;



    private StorageReference storageReference;


    private ItemsPositionGetter mItemsPositionGetter;

    private final VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
            UserDatabaseReference.keepSynced(true);

            storageReference = FirebaseStorage.getInstance().getReference().child(currentUser.getUid()+"/bbb.mp4");


            File videoPath = new File(Environment.getExternalStorageDirectory(),"SoulsSpot");

            if (!videoPath.exists())
            {
                videoPath.mkdirs();
            }


            localeFile = new File(videoPath,"VideoSoul_1.3gp");

            if (localeFile != null)
            {
                try
                {
                    mList.add(ItemFactory.createItemFromStorageUri(mVideoPlayerManager,localeFile,MainActivity.this));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);



        VideoRecyclerViewAdapter videoRecyclerViewAdapter = new VideoRecyclerViewAdapter(mVideoPlayerManager, MainActivity.this, mList);

        mRecyclerView.setAdapter(videoRecyclerViewAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                mScrollState = scrollState;
                if(scrollState == RecyclerView.SCROLL_STATE_IDLE && !mList.isEmpty()){

                    mVideoVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!mList.isEmpty()){
                    mVideoVisibilityCalculator.onScroll(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition() - mLayoutManager.findFirstVisibleItemPosition() + 1,
                            mScrollState);
                }
            }
        });
        mItemsPositionGetter = new RecyclerViewItemPositionGetter(mLayoutManager, mRecyclerView);
    }
    public void takevideo(View view)
    {
        Intent galleryVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryVideoIntent.setType("video/*");
        startActivityForResult(galleryVideoIntent,REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        videoUri = data.getData();

        if (requestCode == REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(this, "Video saved to : \n" + videoUri, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void upload(View view)
    {
        if (storageReference != null)
        {
            UploadTask uploadTask = storageReference.putFile(videoUri);

            uploadTask.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(MainActivity.this, "Upload failed " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Toast.makeText(MainActivity.this, "Upload Completed", Toast.LENGTH_SHORT).show();

                }
            });
        }
        else
        {
            Toast.makeText(this, "Nothing to upload", Toast.LENGTH_SHORT).show();
        }
    }

    public void download(View view)
    {

        try
        {

            File videoPath = new File(Environment.getExternalStorageDirectory(),"SoulsSpot");

            if (!videoPath.exists())
            {
                videoPath.mkdirs();
            }


            localeFile = new File(videoPath,"bbb.mp4");

            Toast.makeText(this, "Video Empty", Toast.LENGTH_SHORT).show();

            storageReference.getFile(localeFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                {
                    Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(MainActivity.this, "Download Failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to create temp file: ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!mList.isEmpty())
        {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {

                    mVideoVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());

                }
            });
        }
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
        else
        {
            UserDatabaseReference.child("online").setValue("true");
        }
    }



    @Override
    protected void onStop()
    {
        super.onStop();

        if(currentUser != null)
        {
            UserDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
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
