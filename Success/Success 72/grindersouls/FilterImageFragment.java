package com.deffe.macros.grindersouls;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.deffe.macros.grindersouls.imageprocessors.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FilterImageFragment extends Fragment implements ThumbnailsAdapter.ThumbnailsAdapterListener
{

    private ThumbnailsAdapter mAdapter;

    private List<ThumbnailItem> thumbnailItemList;;

    private FilterListFragmentListener listener;

    private Context context;

    private Bitmap ImageBitmap;

    public Bitmap getImageBitmap()
    {
        return ImageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap)
    {
        ImageBitmap = imageBitmap;
    }

    public FilterImageFragment()
    {
        // Required empty public constructor
    }

    public void setListener(FilterListFragmentListener listener)
    {
        this.listener = listener;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_filter_image, container, false);

        context = container.getContext();

        thumbnailItemList = new ArrayList<>();

        mAdapter = new ThumbnailsAdapter(getActivity(),thumbnailItemList,this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareThumbnail(getImageBitmap());

        return view;
    }

    public void prepareThumbnail(final Bitmap bitmap) {

        Bitmap thumbImage;

        thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

        if (thumbImage == null) {
            Toast.makeText(context, "ThumbImage Null", Toast.LENGTH_SHORT).show();
            return;
        } else {
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

            t8.filter = SampleFilters.getOldManFilter(context);
            ThumbnailsManager.addThumb(t8);

            t9.filter = SampleFilters.getMarsFilter();
            ThumbnailsManager.addThumb(t9);

            t10.filter = SampleFilters.getRiseFilter(context);
            ThumbnailsManager.addThumb(t10);

            t11.filter = SampleFilters.getAprilFilter(context);
            ThumbnailsManager.addThumb(t11);

            t12.filter = SampleFilters.getAmazonFilter();
            ThumbnailsManager.addThumb(t12);

            t13.filter = SampleFilters.getHaanFilter(context);
            ThumbnailsManager.addThumb(t13);

            t14.filter = SampleFilters.getAdeleFilter();
            ThumbnailsManager.addThumb(t14);

            t15.filter = SampleFilters.getCruzFilter();
            ThumbnailsManager.addThumb(t15);

            t16.filter = SampleFilters.getMetropolis();
            ThumbnailsManager.addThumb(t16);

            t17.filter = SampleFilters.getAudreyFilter();
            ThumbnailsManager.addThumb(t17);

            thumbnailItemList.addAll(ThumbnailsManager.processThumbs(getActivity()));
        }


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onFilterSelected(Filter filter)
    {
        if (listener != null)
        {
            listener.onFilterSelected(filter);
        }
    }

    public interface FilterListFragmentListener
    {
        void onFilterSelected(Filter filter);
    }
}
