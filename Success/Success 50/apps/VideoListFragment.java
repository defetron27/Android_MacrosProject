package com.deffe.macros.apps;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class VideoListFragment extends Fragment {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = VideoListFragment.class.getSimpleName();

    private final ArrayList<BaseVideoItem> mList = new ArrayList<>();

    private final ListItemsVisibilityCalculator mListItemVisibilityCalculator =
            new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mList);

    private ItemsPositionGetter mItemsPositionGetter;


    private final VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {
            if(SHOW_LOGS) Logger.v(TAG, "onPlayerItemChanged " + metaData);

        }
    });

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // if your files are in "assets" directory you can pass AssetFileDescriptor to the VideoPlayerView
        // if they are url's or path values you can pass the String path to the VideoPlayerView
        try {
            mList.add(ItemFactory.createItemFromAsset("video_sample_1.mp4", R.drawable.video_sample_1_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_2.mp4", R.drawable.video_sample_2_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_3.mp4", R.drawable.video_sample_3_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_4.mp4", R.drawable.video_sample_4_pic, getActivity(), mVideoPlayerManager));

            mList.add(ItemFactory.createItemFromAsset("video_sample_1.mp4", R.drawable.video_sample_1_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_2.mp4", R.drawable.video_sample_2_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_3.mp4", R.drawable.video_sample_3_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_4.mp4", R.drawable.video_sample_4_pic, getActivity(), mVideoPlayerManager));

            mList.add(ItemFactory.createItemFromAsset("video_sample_1.mp4", R.drawable.video_sample_1_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_2.mp4", R.drawable.video_sample_2_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_3.mp4", R.drawable.video_sample_3_pic, getActivity(), mVideoPlayerManager));
            mList.add(ItemFactory.createItemFromAsset("video_sample_4.mp4", R.drawable.video_sample_4_pic, getActivity(), mVideoPlayerManager));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        View rootView = inflater.inflate(R.layout.fragment_video_list_view, container, false);

        mListView = (ListView) rootView.findViewById(R.id.list_view);
        VideoListViewAdapter videoListViewAdapter = new VideoListViewAdapter(mVideoPlayerManager, getActivity(), mList);
        mListView.setAdapter(videoListViewAdapter);

        mItemsPositionGetter = new ListViewItemPositionGetter(mListView);
        /**
         * We need to set onScrollListener after we create {@link #mItemsPositionGetter}
         * because {@link AbsListView.OnScrollListener#onScroll(AbsListView, int, int, int)}
         * is called immediately and we will get {@link NullPointerException}
         */
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
                if (scrollState == SCROLL_STATE_IDLE && !mList.isEmpty()) {
                    mListItemVisibilityCalculator.onScrollStateIdle(mItemsPositionGetter, view.getFirstVisiblePosition(), view.getLastVisiblePosition());
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!mList.isEmpty()) {
                    // on each scroll event we need to call onScroll for mListItemVisibilityCalculator
                    // in order to recalculate the items visibility
                    mListItemVisibilityCalculator.onScroll(mItemsPositionGetter, firstVisibleItem, visibleItemCount, mScrollState);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mList.isEmpty()){
            // need to call this method from list view handler in order to have list filled previously

            mListView.post(new Runnable() {
                @Override
                public void run() {

                    mListItemVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mListView.getFirstVisiblePosition(),
                            mListView.getLastVisiblePosition());

                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // we have to stop any playback in onStop
        mVideoPlayerManager.resetMediaPlayer();
    }
}