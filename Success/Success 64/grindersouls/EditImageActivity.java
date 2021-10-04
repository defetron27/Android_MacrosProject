package com.deffe.macros.grindersouls;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.deffe.macros.grindersouls.imageprocessors.Filter;
import com.deffe.macros.grindersouls.imageprocessors.subfilters.BrightnessSubFilter;
import com.deffe.macros.grindersouls.imageprocessors.subfilters.ContrastSubFilter;
import com.deffe.macros.grindersouls.imageprocessors.subfilters.SaturationSubfilter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
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

    private Uri fileUri;

    private static final String IMAGE_DIRECTORY_NAME = "GrindersSouls";

    private DatabaseReference UploadPostReference;

    private StorageReference UploadPostImageStorageRef;

    private FirebaseAuth firebaseAuth;

    private AllPostsRecyclerViewAdapter allPostsRecyclerViewAdapter;

    private Toolbar uploadTodayPostToolbar;

    private String online_user_id;

    private RecyclerView UploadTodayPostRecyclerView;

    private ArrayList<MultiRecyclerModel> UploadedPosts = new ArrayList<>();


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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UploadPostReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(online_user_id);
        UploadPostReference.keepSynced(true);

        UploadPostImageStorageRef = FirebaseStorage.getInstance().getReference().child("Posts");

        UploadPostReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UploadedPosts.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    MultiRecyclerModel model = snapshot.getValue(MultiRecyclerModel.class);

                    UploadedPosts.add(model);
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
        allPostsRecyclerViewAdapter = new AllPostsRecyclerViewAdapter(this,UploadedPosts);
        UploadTodayPostRecyclerView.setAdapter(allPostsRecyclerViewAdapter);
        UploadTodayPostRecyclerView.setHasFixedSize(true);

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

        File file = new File(Environment.getExternalStorageDirectory() , System.currentTimeMillis() + ".jpg");

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
                public void onSuccess(@NonNull final String imagePath)
                {
                    hideLoading();
                    showSnackbar("Image Saved Successfully");

                    mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(imagePath)));

                    final Uri ImageUri = Uri.fromFile(new File(imagePath));

                    DatabaseReference uploadKey = UploadPostReference.push();

                    final String upload_key = uploadKey.getKey();

                    final StorageReference storageReference = UploadPostImageStorageRef.child("Images").child(upload_key+".jpg");

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

                                    UploadPostReference.child(upload_key).child("post").setValue(downloadUrl);
                                    UploadPostReference.child(upload_key).child("type").setValue(0);
                                    UploadPostReference.child(upload_key).child("storage_ref").setValue(ImageUri.toString());
                                    UploadPostReference.child(upload_key).child("uploaded_time").setValue(ServerValue.TIMESTAMP);
                                    UploadPostReference.child(upload_key).child("post_key").setValue(upload_key)
                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                                        {
                                                            @Override
                                                            public void onSuccess(StorageMetadata storageMetadata)
                                                            {
                                                                long sizeInMb = storageMetadata.getSizeBytes();

                                                                String size = getStringSizeFromFile(sizeInMb);

                                                                UploadPostReference.child(upload_key).child("size").setValue(size).addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            Toast.makeText(EditImageActivity.this, "Upload successfully..!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener()
                                                                {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e)
                                                                    {
                                                                        Toast.makeText(EditImageActivity.this, "Error while storing post details, Try again", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener()
                                                        {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e)
                                                            {
                                                                Toast.makeText(EditImageActivity.this, "Size not found", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(EditImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


            ImageView chooseVideoFromGallery = bottomSheet.findViewById(R.id.choose_video_from_gallery);
            ImageView recordVideo = bottomSheet.findViewById(R.id.record_video);

            chooseVideoFromGallery.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent galleryVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryVideoIntent.setType("video/*");
                    galleryVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
                    startActivityForResult(galleryVideoIntent,TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE);
                }
            });

            recordVideo.setOnClickListener(new View.OnClickListener()
            {
                @TargetApi(Build.VERSION_CODES.CUPCAKE)
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

                File file = null;

                if (videoUri != null)
                {
                    file = new File(videoUri.getPath());
                }

                Uri newVideoUri = Uri.fromFile(file);

                Intent videoIntent = new Intent(EditImageActivity.this,VideoPlayActivity.class);
                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                videoIntent.putExtra("videoUri",newVideoUri.toString());
                videoIntent.putExtra("videoOriginalUri",newVideoUri.toString());
                videoIntent.putExtra("purpose","upload");
                videoIntent.putExtra("type","camera");
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
                Uri videoUri = null;

                File file = new File(Environment.getExternalStorageDirectory() + "/GrindersSouls/Grinders Posts/Videos");

                if (!file.exists())
                {
                    file.mkdirs();
                }

                try
                {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                    File mediaFile = new File(file.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

                    videoUri = copyFileToFolder(new File(getFilePathFromUri(EditImageActivity.this, data.getData())), mediaFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();

                    Toast.makeText(this, "Error...copying," + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                if (videoUri != null)
                {
                    Toast.makeText(this, "videoUri not null", Toast.LENGTH_SHORT).show();

                    Intent videoIntent = new Intent(EditImageActivity.this, VideoPlayActivity.class);
                    videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                    videoIntent.putExtra("videoUri", videoUri.toString());
                    videoIntent.putExtra("videoOriginalUri", data.getData().toString());
                    videoIntent.putExtra("type", "gallery");
                    videoIntent.putExtra("purpose","upload");
                    startActivity(videoIntent);
                }
                else
                {
                    Toast.makeText(this, "videoUri null", Toast.LENGTH_SHORT).show();
                }
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
    }

    private Uri copyFileToFolder(File sourceFile, File destinationFile) throws IOException
    {
        FileChannel source;
        FileChannel destination;

        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destinationFile).getChannel();

        if (source != null)
        {
            destination.transferFrom(source,0,source.size());
        }

        if (source != null)
        {
            source.close();
        }
        destination.close();

        return Uri.fromFile(destinationFile);
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

    @TargetApi(Build.VERSION_CODES.FROYO)
    private static File getOutputMediaFile(int type)
    {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/GrindersSouls/Grinders Post","Videos");

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

    private String getFilePathFromUri(Context context,Uri contentUri)
    {
        String filePath = null;

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat)
        {
            filePath = generateFromKitKat(context, contentUri);
        }

        if (filePath != null)
        {
            return filePath;
        }

        Cursor cursor = context.getContentResolver().query(contentUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                filePath = cursor.getString(column_index);
            }

            cursor.close();
        }
        return filePath == null ? contentUri.getPath() : filePath;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String generateFromKitKat(Context context, Uri contentUri)
    {
        String filePath = null;

        if (DocumentsContract.isDocumentUri(context,contentUri))
        {
            String wholeID = DocumentsContract.getDocumentId(contentUri);

            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Video.Media.DATA};
            String sel = MediaStore.Video.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,column,sel,new String[]{id},null);

            int columnIndex = 0;

            if (cursor != null)
            {
                columnIndex = cursor.getColumnIndex(column[0]);
            }

            if (cursor != null && cursor.moveToFirst())
            {
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }

        }

        return filePath;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private boolean isDeviceSupportCamera()
    {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public class AllPostsRecyclerViewAdapter extends RecyclerView.Adapter {
        Context context;
        private ArrayList<MultiRecyclerModel> UploadedPosts;


        AllPostsRecyclerViewAdapter(Context context, ArrayList<MultiRecyclerModel> uploadedPosts) {
            this.context = context;
            UploadedPosts = uploadedPosts;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_post_image_items, parent, false);

                return new ImageTypeViewHolder(view);
            } else if (viewType == 1) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_post_video_items, parent, false);

                return new VideoTypeViewHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            final MultiRecyclerModel object = UploadedPosts.get(position);

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            final DatabaseReference uploadPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(online_user_id);
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

            if (object != null) {
                switch (object.getType()) {
                    case 0:

                        ((ImageTypeViewHolder) holder).ImageOnlineUserName.setVisibility(View.VISIBLE);
                        ((ImageTypeViewHolder) holder).ImagePostUploadTime.setVisibility(View.VISIBLE);
                        ((ImageTypeViewHolder) holder).uploaded_img.setVisibility(View.VISIBLE);
                        ((ImageTypeViewHolder) holder).thumb_img.setVisibility(View.VISIBLE);
                        ((ImageTypeViewHolder) holder).ImageLine1.setVisibility(View.VISIBLE);
                        ((ImageTypeViewHolder) holder).ImageLine2.setVisibility(View.VISIBLE);
                        ((ImageTypeViewHolder) holder).RemovePostImage.setVisibility(View.VISIBLE);

                        usersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.child("user_name").getValue().toString();
                                String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();


                                ((ImageTypeViewHolder) holder).ImageOnlineUserName.setText(name);
                                ((ImageTypeViewHolder) holder).setUser_Image_thumb_img(context, thumbImage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        uploadPostsRef.child(object.getPost_key()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long upload_time = object.getUploaded_time();

                                String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                                Picasso.with(context).load( object.getStorage_ref()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                                        .into(((ImageTypeViewHolder) holder).uploaded_img, new Callback()
                                        {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(context).load( object.getStorage_ref()).placeholder(R.drawable.vadim).into(((ImageTypeViewHolder) holder).uploaded_img);
                                            }
                                        });

                                ((ImageTypeViewHolder) holder).uploaded_img.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent imageIntent = new Intent(EditImageActivity.this,PostImageActivity.class);
                                        imageIntent.putExtra("imageUri",object.getStorage_ref());
                                        imageIntent.putExtra("purpose","view");
                                        startActivity(imageIntent);
                                    }
                                });

                                ((ImageTypeViewHolder) holder).ImagePostUploadTime.setText(postUploadedTime);

                                ((ImageTypeViewHolder) holder).RemovePostImage.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        PopupMenu popupMenu = new PopupMenu(context,((ImageTypeViewHolder) holder).RemovePostImage);

                                        popupMenu.inflate(R.menu.remove_post_menu);

                                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                                        {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item)
                                            {
                                                AlertDialog.Builder confirm = new AlertDialog.Builder(EditImageActivity.this);

                                                confirm.setTitle("Do you want delete post?");

                                                confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(object.getPost());

                                                        uploadPostsRef.child(object.getPost_key()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(context, "Image post removed", Toast.LENGTH_SHORT).show();

                                                                    storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                Toast.makeText(context, "Storage image deleted", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener()
                                                                    {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e)
                                                                        {
                                                                            Toast.makeText(context, "Failed to delete storage image " + e.getMessage() , Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener()
                                                        {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e)
                                                            {
                                                                Toast.makeText(context, "Failed to delete image post " + e.getMessage() , Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });

                                                confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        dialog.dismiss();
                                                    }
                                                });

                                                confirm.show();

                                                return true;
                                            }
                                        });
                                        popupMenu.show();
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        break;
                    case 1:

                        usersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.child("user_name").getValue().toString();
                                String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();


                                ((VideoTypeViewHolder) holder).VideoOnlineUserName.setText(name);
                                ((VideoTypeViewHolder) holder).setUser_Video_thumb_img(context, thumbImage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        uploadPostsRef.child(object.getPost_key()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long upload_time = object.getUploaded_time();

                                String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                                GlideApp.with(context).load(object.getStorage_ref()).centerCrop().into(((VideoTypeViewHolder) holder).videoThumbImage);

                                ((VideoTypeViewHolder) holder).videoThumbImage.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent videoIntent = new Intent(EditImageActivity.this,VideoPlayActivity.class);
                                        videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                        videoIntent.putExtra("purpose","play");
                                        videoIntent.putExtra("videoUri", object.getStorage_ref());
                                        startActivity(videoIntent);
                                    }
                                });

                                ((VideoTypeViewHolder) holder).VideoPostUploadTime.setText(postUploadedTime);

                                ((VideoTypeViewHolder) holder).VideoPlayButton.setVisibility(View.VISIBLE);

                                ((VideoTypeViewHolder) holder).RemoveVideoPost.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        PopupMenu popupMenu = new PopupMenu(context,((VideoTypeViewHolder) holder).RemoveVideoPost);

                                        popupMenu.inflate(R.menu.remove_post_menu);

                                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                                        {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item)
                                            {
                                                AlertDialog.Builder confirm = new AlertDialog.Builder(EditImageActivity.this);

                                                confirm.setTitle("Do you want delete post?");

                                                confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(object.getPost());

                                                        uploadPostsRef.child(object.getPost_key()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(context, "Video post removed", Toast.LENGTH_SHORT).show();

                                                                    storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                Toast.makeText(context, "Storage video deleted", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener()
                                                                    {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e)
                                                                        {
                                                                            Toast.makeText(context, "Failed to delete storage video " + e.getMessage() , Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener()
                                                        {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e)
                                                            {
                                                                Toast.makeText(context, "Failed to delete video post " + e.getMessage() , Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });

                                                confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        dialog.dismiss();
                                                    }
                                                });

                                                confirm.show();

                                                return true;
                                            }
                                        });
                                        popupMenu.show();
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return UploadedPosts.size();
        }

        @Override
        public int getItemViewType(int position)
        {
            int i = UploadedPosts.get(position).getType();

            switch (i) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                default:
                    return -1;
            }
        }

        public class ImageTypeViewHolder extends RecyclerView.ViewHolder
        {
            ImageView uploaded_img;

            TextView ImageOnlineUserName;

            TextView ImagePostUploadTime;

            View v;

            ImageButton RemovePostImage;

            TextView ImageLine1,ImageLine2;

            CircleImageView thumb_img;

            ImageTypeViewHolder(View itemView) {
                super(itemView);

                v = itemView;

                ImageOnlineUserName = v.findViewById(R.id.image_post_user_name);

                ImagePostUploadTime = v.findViewById(R.id.image_post_user_time);

                uploaded_img = v.findViewById(R.id.image_post_image_view);

                RemovePostImage = v.findViewById(R.id.image_post_remove_button);

                ImageLine1 = v.findViewById(R.id.image_line1);

                ImageLine2 = v.findViewById(R.id.image_line2);

                thumb_img = v.findViewById(R.id.image_post_user_image);
            }

            void setUser_Image_thumb_img(final Context context, final String user_thumb_img)
            {
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
        }

        public class VideoTypeViewHolder extends RecyclerView.ViewHolder {
            TextView VideoOnlineUserName;

            TextView VideoPostUploadTime;

            View v;

            ImageView videoThumbImage;

            ImageButton RemoveVideoPost;

            ImageView VideoPlayButton;

            VideoTypeViewHolder(View itemView) {
                super(itemView);

                v = itemView;

                VideoOnlineUserName = v.findViewById(R.id.video_post_user_name);

                VideoPostUploadTime = v.findViewById(R.id.video_post_user_time);

                videoThumbImage = v.findViewById(R.id.video_thumb_view);

                RemoveVideoPost = v.findViewById(R.id.video_post_remove_button);

                VideoPlayButton = v.findViewById(R.id.video_play_button);
            }

            void setUser_Video_thumb_img(final Context context, final String user_thumb_img) {
                final CircleImageView thumb_img = v.findViewById(R.id.video_post_user_image);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
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
    }

    public static String getStringSizeFromFile(long size)
    {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeMb;
        float sizeTera = sizeGb * sizeGb;

        if (size < sizeMb)
        {
            return decimalFormat.format(size / sizeKb) + " Kb";
        }
        else if (size < sizeGb)
        {
            return decimalFormat.format(size / sizeMb) + " Mb";
        }
        else if (size < sizeTera)
        {
            return decimalFormat.format(size / sizeGb) + " Gb";
        }
        return "";
    }
}