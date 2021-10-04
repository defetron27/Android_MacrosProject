package com.deffe.macros.imageprocess;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.deffe.macros.imageprocess.imageprocessors.Filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class EditImageActivity extends BaseActivity implements OnPhotoEditorListener, View.OnClickListener, PropertiesBSFragment.Properties, EmojiBSFragment.EmojiListener, StickerBSFragment.StickerListener,ThumbnailCallback
{

    static
    {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = EditImageActivity.class.getSimpleName();
    public static final String EXTRA_IMAGE_PATHS = "extra_image_paths";
    private static final int CAMERA_REQUEST = 52;
    private static final int PICK_REQUEST = 53;
    private PhotoEditor mPhotoEditor;
    private PhotoEditorView mPhotoEditorView;
    private PropertiesBSFragment mPropertiesBSFragment;
    private EmojiBSFragment mEmojiBSFragment;
    private StickerBSFragment mStickerBSFragment;
    private Typeface mWonderFont;
    private ImageView stickerView;
    private ImageView imgEraser;
    private ImageView imgPencil;
    private ImageView imgFilter;


    private Activity activity;
    private RecyclerView thumbListView;



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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image);

        activity = this;

        thumbListView = findViewById(R.id.thumbnails);
        mPhotoEditorView.getSource().setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.photo), 640, 640, false));

        initHorizontalList();

        mPhotoEditorView = findViewById(R.id.photoEditorView);

        stickerView = findViewById(R.id.imgSticker);

        imgPencil = findViewById(R.id.imgPencil);

        imgEraser = findViewById(R.id.btnEraser);

        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf");

        mPropertiesBSFragment = new PropertiesBSFragment();
        mEmojiBSFragment = new EmojiBSFragment();
        mStickerBSFragment = new StickerBSFragment();
        mStickerBSFragment.setStickerListener(this);
        mEmojiBSFragment.setEmojiListener(this);
        mPropertiesBSFragment.setPropertiesChangeListener(this);

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

        //Set Image Dynamically
        //mPhotoEditorView.getSource().setImageResource(R.drawable.got);
    }

    private void initHorizontalList()
    {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        thumbListView.setLayoutManager(layoutManager);
        thumbListView.setHasFixedSize(true);
        bindDataToAdapter();
    }

    private void bindDataToAdapter() {
        final Context context = this.getApplication();
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.photo), 640, 640, false);
                ThumbnailItem t1 = new ThumbnailItem();
                ThumbnailItem t2 = new ThumbnailItem();
                ThumbnailItem t3 = new ThumbnailItem();
                ThumbnailItem t4 = new ThumbnailItem();
                ThumbnailItem t5 = new ThumbnailItem();
                ThumbnailItem t6 = new ThumbnailItem();
                ThumbnailItem t7 = new ThumbnailItem();
                ThumbnailItem t8 = new ThumbnailItem();
                ThumbnailItem t9 = new ThumbnailItem();
                ThumbnailItem t10 = new ThumbnailItem();
                ThumbnailItem t11 = new ThumbnailItem();
                ThumbnailItem t12 = new ThumbnailItem();
                ThumbnailItem t13 = new ThumbnailItem();
                ThumbnailItem t14 = new ThumbnailItem();
                ThumbnailItem t15 = new ThumbnailItem();
                ThumbnailItem t16 = new ThumbnailItem();
                ThumbnailItem t17 = new ThumbnailItem();


                t1.image = thumbImage;
                t2.image = thumbImage;
                t3.image = thumbImage;
                t4.image = thumbImage;
                t5.image = thumbImage;
                t6.image = thumbImage;
                t7.image = thumbImage;
                t8.image = thumbImage;
                t9.image = thumbImage;
                t10.image = thumbImage;
                t11.image = thumbImage;
                t12.image = thumbImage;
                t13.image = thumbImage;
                t14.image = thumbImage;
                t15.image = thumbImage;
                t16.image = thumbImage;
                t17.image = thumbImage;


                ThumbnailsManager.clearThumbs();
                ThumbnailsManager.addThumb(t1); // Original Image

                t2.filter = SampleFilters.getStarLitFilter();
                ThumbnailsManager.addThumb(t2);

                t3.filter = SampleFilters.getBlueMessFilter();
                ThumbnailsManager.addThumb(t3);

                t4.filter = SampleFilters.getAweStruckVibeFilter();
                ThumbnailsManager.addThumb(t4);

                t5.filter = SampleFilters.getLimeStutterFilter();
                ThumbnailsManager.addThumb(t5);

                t6.filter = SampleFilters.getNightWhisperFilter();
                ThumbnailsManager.addThumb(t6);

                t7.filter = SampleFilters.getClarendon();
                ThumbnailsManager.addThumb(t7);

                t8.filter = SampleFilters.getOldManFilter(EditImageActivity.this);
                ThumbnailsManager.addThumb(t8);

                t9.filter = SampleFilters.getMarsFilter();
                ThumbnailsManager.addThumb(t9);

                t10.filter = SampleFilters.getRiseFilter(EditImageActivity.this);
                ThumbnailsManager.addThumb(t10);

                t11.filter = SampleFilters.getAprilFilter(EditImageActivity.this);
                ThumbnailsManager.addThumb(t11);

                t12.filter = SampleFilters.getAmazonFilter();
                ThumbnailsManager.addThumb(t12);

                t13.filter = SampleFilters.getHaanFilter(EditImageActivity.this);
                ThumbnailsManager.addThumb(t13);

                t14.filter = SampleFilters.getAdeleFilter();
                ThumbnailsManager.addThumb(t14);

                t15.filter = SampleFilters.getCruzFilter();
                ThumbnailsManager.addThumb(t15);

                t16.filter = SampleFilters.getMetropolis();
                ThumbnailsManager.addThumb(t16);

                t17.filter = SampleFilters.getAudreyFilter();
                ThumbnailsManager.addThumb(t17);


                List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) activity);
                thumbListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }


    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode)
    {
        TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor()
        {
            @Override
            public void onDone(String inputText, int colorCode)
            {
                mPhotoEditor.editText(rootView, inputText, colorCode);
            }
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews)
    {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews)
    {
        Log.d(TAG, "onRemoveViewListener() called with: numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType)
    {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType)
    {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.imgPencil:
                imgPencil.setBackgroundColor(getResources().getColor(R.color.item_background));
                stickerView.setBackgroundColor(Color.BLACK);
                imgEraser.setBackgroundColor(Color.BLACK);
                thumbListView.setVisibility(View.GONE);
                mPhotoEditor.setBrushDrawingMode(true);
                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
                break;

            case R.id.btnEraser:
                imgEraser.setBackgroundColor(getResources().getColor(R.color.item_background));
                imgPencil.setBackgroundColor(Color.BLACK);
                stickerView.setBackgroundColor(Color.BLACK);
                thumbListView.setVisibility(View.GONE);
                mPhotoEditor.brushEraser();
                break;

            case R.id.imgSticker:
                stickerView.setBackgroundColor(getResources().getColor(R.color.item_background));
                imgPencil.setBackgroundColor(Color.BLACK);
                imgEraser.setBackgroundColor(Color.BLACK);
                thumbListView.setVisibility(View.GONE);
                mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());
                break;

            case R.id.imgFilter:
                thumbListView.setVisibility(View.VISIBLE);
                imgFilter.setBackgroundColor(getResources().getColor(R.color.item_background));
                imgPencil.setBackgroundColor(Color.BLACK);
                stickerView.setBackgroundColor(Color.BLACK);
                imgEraser.setBackgroundColor(Color.BLACK);
                break;

            case R.id.imgText:
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor()
                {
                    @Override
                    public void onDone(String inputText, int colorCode)
                    {
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

            case R.id.imgSave:
                saveImage();
                break;

            case R.id.imgEmoji:
                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
                break;

            case R.id.imgCamera:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                break;

            case R.id.imgGallery:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void saveImage()
    {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            showLoading();
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + ""
                    + System.currentTimeMillis() + ".png");
            try
            {
                file.createNewFile();
                mPhotoEditor.saveImage(file.getAbsolutePath(), new PhotoEditor.OnSaveListener()
                {
                    @Override
                    public void onSuccess(@NonNull String imagePath)
                    {
                        hideLoading();
                        showSnackbar("Image Saved Successfully");
                        mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(imagePath)));
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception)
                    {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    mPhotoEditor.clearAllViews();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    mPhotoEditorView.getSource().setImageBitmap(photo);
                    break;
                case PICK_REQUEST:
                    try {
                        mPhotoEditor.clearAllViews();
                        Uri uri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        mPhotoEditorView.getSource().setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
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

    @Override
    public void onThumbnailClick(Filter filter)
    {
        mPhotoEditorView.getSource().setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.photo), 640, 640, false)));
    }
}
