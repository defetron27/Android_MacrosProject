package com.deffe.macros.imageprocess;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.deffe.macros.imageprocess.imageprocessors.Filter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ThumbnailCallback
{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    private Activity activity;
    private RecyclerView thumbListView;
    private ImageView placeHolderImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        initUIWidgets();
    }

    private void initUIWidgets() {
        thumbListView = (RecyclerView) findViewById(R.id.thumbnails);
        placeHolderImageView = (ImageView) findViewById(R.id.place_holder_imageview);
        placeHolderImageView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.photo), 640, 640, false));
        initHorizontalList();
    }

    private void initHorizontalList() {
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

                t8.filter = SampleFilters.getOldManFilter(MainActivity.this);
                ThumbnailsManager.addThumb(t8);

                t9.filter = SampleFilters.getMarsFilter();
                ThumbnailsManager.addThumb(t9);

                t10.filter = SampleFilters.getRiseFilter(MainActivity.this);
                ThumbnailsManager.addThumb(t10);

                t11.filter = SampleFilters.getAprilFilter(MainActivity.this);
                ThumbnailsManager.addThumb(t11);

                t12.filter = SampleFilters.getAmazonFilter();
                ThumbnailsManager.addThumb(t12);

                t13.filter = SampleFilters.getHaanFilter(MainActivity.this);
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
    public void onThumbnailClick(Filter filter) {
        placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.photo), 640, 640, false)));
    }
}
