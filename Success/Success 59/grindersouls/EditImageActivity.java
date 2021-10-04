package com.deffe.macros.grindersouls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deffe.macros.grindersouls.imageprocessors.Filter;
import com.deffe.macros.grindersouls.imageprocessors.subfilters.BrightnessSubFilter;
import com.deffe.macros.grindersouls.imageprocessors.subfilters.ContrastSubFilter;
import com.deffe.macros.grindersouls.imageprocessors.subfilters.SaturationSubfilter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditImageActivity extends BaseActivity implements
        OnPhotoEditorListener,
        View.OnClickListener,
        PropertiesBSFragment.Properties,
        EmojiBSFragment.EmojiListener,
        StickerBSFragment.StickerListener,
        FilterImageFragment.FilterListFragmentListener,
        CustomFilterFragment.CustomFilterFragmentListener {

    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = EditImageActivity.class.getSimpleName();
    public static final String EXTRA_IMAGE_PATHS = "extra_image_paths";

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE = 300;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private ImageView ChooseVideoFromGallery,RecordVideo;

    private Uri fileUri;

    private static final String IMAGE_DIRECTORY_NAME = "GrindersSouls";

    private DatabaseReference UploadPostReference,UploadUserReference;

    private StorageReference UploadPostImageStorageRef;

    private FirebaseAuth firebaseAuth;

    private AllPostsRecyclerViewAdapter allPostsRecyclerViewAdapter;

    private Toolbar uploadTodayPostToolbar;

    private String online_user_id;

    private RecyclerView UploadTodayPostRecyclerView;

    private ArrayList<String> UploadedPosts = new ArrayList<>();


    private PhotoEditor mPhotoEditor;
    private PhotoEditorView mPhotoEditorView;
    private PropertiesBSFragment mPropertiesBSFragment;
    private EmojiBSFragment mEmojiBSFragment;
    private StickerBSFragment mStickerBSFragment;
    private Typeface mWonderFont;

    private ImageView stickerView;
    private ImageView imgEraser;
    private ImageView imgPencil;
    private ImageView imgEmoji;
    private ImageView imgText;
    private ImageView btnUndo;
    private ImageView btnRedo;

    private EditText AboutTheImage;

    private FloatingActionButton ImageOkButton;

    private Bitmap originalImage;

    private Bitmap filteredImage;

    private ViewPager viewPager;

    private TabLayout tabLayout;

    private Bitmap finalImage;

    private FilterImageFragment filterImageFragment;
    private CustomFilterFragment customFilterFragment;

    private int brightnessFinal = 0;
    private float saturationFinal = 1.0f;
    private float contrastFinal = 1.0f;

    public static void launch(Context context, ArrayList<String> imagesPath) {
        Intent starter = new Intent(context, EditImageActivity.class);
        starter.putExtra(EXTRA_IMAGE_PATHS, imagesPath);
        context.startActivity(starter);
    }

    public static void launch(Context context, String imagePath) {
        ArrayList<String> imagePaths = new ArrayList<>();
        imagePaths.add(imagePath);
        launch(context, imagePaths);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image);

        mPhotoEditorView = findViewById(R.id.photoEditorView);

        tabLayout = findViewById(R.id.tabs);

        viewPager = findViewById(R.id.viewpager);

        ImageOkButton = findViewById(R.id.upload_today_post);

        stickerView = findViewById(R.id.imgSticker);

        imgPencil = findViewById(R.id.imgPencil);

        imgEraser = findViewById(R.id.btnEraser);

        imgEmoji = findViewById(R.id.imgEmoji);

        imgText = findViewById(R.id.imgText);

        btnUndo = findViewById(R.id.imgUndo);

        btnRedo = findViewById(R.id.imgRedo);

        AboutTheImage = findViewById(R.id.about_the_image);

        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf");

        mPropertiesBSFragment = new PropertiesBSFragment();
        mEmojiBSFragment = new EmojiBSFragment();
        mStickerBSFragment = new StickerBSFragment();
        mStickerBSFragment.setStickerListener(this);
        mEmojiBSFragment.setEmojiListener(this);
        mPropertiesBSFragment.setPropertiesChangeListener(this);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        uploadTodayPostToolbar = findViewById(R.id.upload_today_posts_toolbar);
        setSupportActionBar(uploadTodayPostToolbar);
        getSupportActionBar().setTitle("Upload today post");

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UploadPostReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(online_user_id);
        UploadPostReference.keepSynced(true);

        UploadUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        UploadUserReference.keepSynced(true);

        UploadPostImageStorageRef = FirebaseStorage.getInstance().getReference().child("Upload_Posts_Pictures");

        UploadPostReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UploadedPosts.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    UploadedPosts.add(snapshot.getKey());
                }

                allPostsRecyclerViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        UploadTodayPostRecyclerView = findViewById(R.id.upload_today_post_recycler_view);
        UploadTodayPostRecyclerView.setLayoutManager(new LinearLayoutManager(EditImageActivity.this, LinearLayoutManager.VERTICAL, false));
        allPostsRecyclerViewAdapter = new AllPostsRecyclerViewAdapter(this);
        UploadTodayPostRecyclerView.setAdapter(allPostsRecyclerViewAdapter);



        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView).setPinchTextScalable(true) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this);

        stickerView.setOnClickListener(this);
        imgEraser.setOnClickListener(this);
        imgPencil.setOnClickListener(this);
        imgText.setOnClickListener(this);
        imgEmoji.setOnClickListener(this);
        btnRedo.setOnClickListener(this);
        btnUndo.setOnClickListener(this);

        //Set Image Dynamically
        //mPhotoEditorView.getSource().setImageResource(R.drawable.got);

        ImageOkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                saveImage();

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if (inputMethodManager != null)
                {
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager)
    {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filterImageFragment = new FilterImageFragment();
        filterImageFragment.setListener(this);

        customFilterFragment = new CustomFilterFragment();
        customFilterFragment.setListener(this);

        adapter.addFragment(filterImageFragment, getString(R.string.tab_filters));
        adapter.addFragment(customFilterFragment, getString(R.string.tab_edit));

        viewPager.setAdapter(adapter);
    }


    private class AllPostsRecyclerViewAdapter extends RecyclerView.Adapter<AllPostsRecyclerViewAdapter.PostsViewHolder>
    {

        Context context;

        AllPostsRecyclerViewAdapter(Context context)
        {
            this.context = context;
        }


        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_today_post_items, parent, false);

            return new PostsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PostsViewHolder holder, int position)
        {
            final String post = UploadedPosts.get(position);

            UploadUserReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    String name = dataSnapshot.child("user_name").getValue().toString();
                    String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                    holder.onlineUserName.setText(name);
                    holder.setUser_thumb_img(context, thumbImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            UploadPostReference.child(post).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    String img = dataSnapshot.child("posted_image").getValue().toString();

                    String time = dataSnapshot.child("uploaded_time").getValue().toString();

                    long upload_time = Long.parseLong(time);

                    String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, EditImageActivity.this);

                    holder.setUser_Uploaded_img(context,img);

                    holder.postUploadTime.setText(postUploadedTime);

                    if (dataSnapshot.hasChild("about_the_image"))
                    {
                        String description = dataSnapshot.child("about_the_image").getValue().toString();

                        holder.Description.setVisibility(View.VISIBLE);
                        holder.Description.setText(description);
                    }
                    else
                    {
                        holder.Description.setVisibility(View.GONE);
                    }
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
            return UploadedPosts.size();
        }

        class PostsViewHolder extends RecyclerView.ViewHolder
        {
            TextView onlineUserName;

            TextView postUploadTime;

            TextView Description;

            View v;

            PostsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                onlineUserName = v.findViewById(R.id.upload_post_online_user_name);

                postUploadTime = v.findViewById(R.id.uploaded_post_online_user_time);

                Description = v.findViewById(R.id.decription);
            }

            void setUser_thumb_img(final Context context, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.upload_post_online_user_image);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(thumb_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }

            void setUser_Uploaded_img(final Context context, final String user_thumb_img)
            {
                final ImageView uploaded_img = v.findViewById(R.id.upload_post_image_view);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(uploaded_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(uploaded_img);
                            }
                        });
            }
        }
    }



    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);

        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
        TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
            @Override
            public void onDone(String inputText, int colorCode) {
                mPhotoEditor.editText(rootView, inputText, colorCode);
            }
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {
        Log.d(TAG, "onRemoveViewListener() called with: numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgPencil:
                mPhotoEditor.setBrushDrawingMode(true);
                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
                break;

            case R.id.btnEraser:
                mPhotoEditor.brushEraser();
                break;

            case R.id.imgSticker:
                mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());
                break;

            case R.id.imgEmoji:
                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
                break;

            case R.id.imgText:
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
                    @Override
                    public void onDone(String inputText, int colorCode) {
                        mPhotoEditor.addText(inputText, colorCode);
                    }
                });
                break;

            case R.id.imgUndo:
                mPhotoEditor.undo();
                break;

            case R.id.imgRedo:
                mPhotoEditor.redo();
                break;
        }
    }

    private void saveImage()
    {
        showLoading();
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + ""
                + System.currentTimeMillis() + ".jpg");
        try
        {
            file.createNewFile();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mPhotoEditor.saveImage(file.getAbsolutePath(), new PhotoEditor.OnSaveListener()
            {
                @Override
                public void onSuccess(@NonNull String imagePath)
                {
                    hideLoading();
                    showSnackbar("Image Saved Successfully");

                    mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(imagePath)));

                    Uri ImageUri = Uri.fromFile(new File(imagePath));

                    DatabaseReference uploadKey = UploadPostReference.push();

                    final String upload_key = uploadKey.getKey();

                    final String aboutTheImage = AboutTheImage.getText().toString();

                    StorageReference storageReference = UploadPostImageStorageRef.child(upload_key+".jpg");

                    if (ImageUri != null)
                    {
                        storageReference.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                            {
                                if (task.isSuccessful())
                                {
                                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                    UploadPostReference.child(upload_key).child("posted_image").setValue(downloadUrl);
                                    UploadPostReference.child(upload_key).child("uploaded_time").setValue(ServerValue.TIMESTAMP)
                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        if (!aboutTheImage.equals(""))
                                                        {
                                                            UploadPostReference.child(upload_key).child("about_the_image").setValue(aboutTheImage);

                                                            Toast.makeText(EditImageActivity.this, "Upload successfully..!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                        uploadTodayPostToolbar.setVisibility(View.VISIBLE);
                        UploadTodayPostRecyclerView.setVisibility(View.VISIBLE);

                        mPhotoEditorView.setVisibility(View.GONE);
                        stickerView.setVisibility(View.GONE);
                        imgPencil.setVisibility(View.GONE);
                        imgEmoji.setVisibility(View.GONE);
                        imgEraser.setVisibility(View.GONE);
                        imgText.setVisibility(View.GONE);
                        viewPager.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.GONE);
                        btnUndo.setVisibility(View.GONE);
                        btnRedo.setVisibility(View.GONE);
                        ImageOkButton.setVisibility(View.GONE);
                        AboutTheImage.setVisibility(View.GONE);

                        AboutTheImage.setText("");

                        filterImageFragment.getImageBitmap().recycle();

                        allPostsRecyclerViewAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    hideLoading();
                    showSnackbar("Failed to save Image");
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onColorChanged(int colorCode)
    {
        mPhotoEditor.setBrushColor(colorCode);
    }

    @Override
    public void onOpacityChanged(int opacity)
    {
        mPhotoEditor.setOpacity(opacity);
    }

    @Override
    public void onBrushSizeChanged(int brushSize)
    {
        mPhotoEditor.setBrushSize(brushSize);
    }

    @Override
    public void onEmojiClick(String emojiUnicode)
    {
        mPhotoEditor.addEmoji(emojiUnicode);

    }

    @Override
    public void onStickerClick(Bitmap bitmap)
    {
        mPhotoEditor.addImage(bitmap);
    }

    @Override
    public void isPermissionGranted(boolean isGranted, String permission)
    {
        if (isGranted)
        {
            saveImage();
        }
    }

    private void resetControls()
    {
        if (customFilterFragment != null)
        {
            customFilterFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
    }
    @Override
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        mPhotoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        mPhotoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        mPhotoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        // once the editing is done i.e seekbar is drag is completed,
        // apply the values on to filtered image
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalImage = myFilter.processFilter(bitmap);
    }


    @Override
    public void onFilterSelected(Filter filter)
    {
        resetControls();

        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        mPhotoEditorView.getSource().setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                uploadTodayPostToolbar.setVisibility(View.GONE);
                UploadTodayPostRecyclerView.setVisibility(View.GONE);

                mPhotoEditorView.setVisibility(View.VISIBLE);
                stickerView.setVisibility(View.VISIBLE);
                imgPencil.setVisibility(View.VISIBLE);
                imgEmoji.setVisibility(View.VISIBLE);
                imgEraser.setVisibility(View.VISIBLE);
                imgText.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                btnUndo.setVisibility(View.VISIBLE);
                btnRedo.setVisibility(View.VISIBLE);
                ImageOkButton.setVisibility(View.VISIBLE);
                AboutTheImage.setVisibility(View.VISIBLE);

                mPhotoEditor.clearAllViews();

                Uri resultUri = result.getUri();

                Bitmap bitmap = null;
                try
                {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

                mPhotoEditorView.getSource().setImageBitmap(originalImage);
                bitmap.recycle();

                // render selected image thumbnails
                filterImageFragment.setImageBitmap(originalImage);
            }
        }
        else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri videoUri = data.getData();

                Toast.makeText(this, videoUri.toString(), Toast.LENGTH_SHORT).show();

                Intent videoIntent = new Intent(EditImageActivity.this,VideoPlayActivity.class);
                videoIntent.putExtra("videoUri",videoUri.toString());
                startActivity(videoIntent);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "User cancelled video recording", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Failed to record video", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri videoUri = data.getData();

                Toast.makeText(this, videoUri.toString(), Toast.LENGTH_SHORT).show();

                Intent videoIntent = new Intent(EditImageActivity.this,VideoPlayActivity.class);
                videoIntent.putExtra("videoUri",videoUri.toString());
                startActivity(videoIntent);

            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.upload_post_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.upload_image)
        {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1 ,1)
                    .start(EditImageActivity.this);
        }
        if (item.getItemId() == R.id.upload_video)
        {
            View view1 = getLayoutInflater().inflate(R.layout.bottom_sheet,null);

            final Dialog bottomSheet = new Dialog(EditImageActivity.this,R.style.MaterialDialogSheet);
            bottomSheet.setContentView(view1);
            bottomSheet.setCancelable(true);
            bottomSheet.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            bottomSheet.getWindow().setGravity(Gravity.BOTTOM);
            bottomSheet.show();


            ChooseVideoFromGallery = bottomSheet.findViewById(R.id.choose_video_from_gallery);
            RecordVideo = bottomSheet.findViewById(R.id.record_video);

            ChooseVideoFromGallery.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent galleryVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryVideoIntent.setType("video/*");
                    startActivityForResult(galleryVideoIntent,TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE);
                }
            });

            RecordVideo.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

                    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    startActivityForResult(videoIntent,CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
                }
            });
        }


        if (!isDeviceSupportCamera())
        {
            Toast.makeText(this, "Your does not support camera", Toast.LENGTH_SHORT).show();

            finish();
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)
    {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    public Uri getOutputMediaFileUri(int type)
    {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type)
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d(IMAGE_DIRECTORY_NAME,"Oops! failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }
        else
        {
            return null;
        }

        return mediaFile;
    }


    private boolean isDeviceSupportCamera()
    {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}