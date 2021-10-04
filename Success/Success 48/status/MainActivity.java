package com.deffe.macros.status;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = MainActivity.class.getSimpleName();



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



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        try
        {
            mList.add(ItemFactory.createItemFromUrl(mVideoPlayerManager,"https://firebasestorage.googleapis.com/v0/b/status2-57fd1.appspot.com/o/bbb.mp4?alt=media&token=b0d8873b-3551-4de6-8980-608d62cded0f"));
            mList.add(ItemFactory.createItemFromUrl(mVideoPlayerManager,"https://firebasestorage.googleapis.com/v0/b/status2-57fd1.appspot.com/o/cosmos.mp4?alt=media&token=c7ee5da3-a15f-4dae-8a8d-d16bb17d6ae0"));
            mList.add(ItemFactory.createItemFromUrl(mVideoPlayerManager,"https://firebasestorage.googleapis.com/v0/b/status2-57fd1.appspot.com/o/tos.mp4?alt=media&token=7490540a-efb7-4f76-acb5-a4b66b4b67be"));

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }


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
    public void onStop()
    {
        super.onStop();
        mVideoPlayerManager.resetMediaPlayer();
    }
}
