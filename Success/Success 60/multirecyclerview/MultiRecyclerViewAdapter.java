package com.deffe.max.multirecyclerview;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import java.util.ArrayList;

public class MultiRecyclerViewAdapter extends RecyclerView.Adapter
{
    private ArrayList<Model> dataSet;
    private Context context;
    private int total_types;

    private SimpleExoPlayer exoPlayer;

    public static class TextTypeViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;

        TextTypeViewHolder(View itemView)
        {
            super(itemView);

            this.textView = itemView.findViewById(R.id.text_view);
        }
    }

    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;

        ImageTypeViewHolder(View itemView)
        {
            super(itemView);

            this.imageView = itemView.findViewById(R.id.image_view);
        }
    }

    public static class VideoTypeViewHolder extends RecyclerView.ViewHolder
    {
        SimpleExoPlayerView simpleExoPlayerView;

        VideoTypeViewHolder(View itemView)
        {
            super(itemView);

            this.simpleExoPlayerView = itemView.findViewById(R.id.exo_player_view);
        }
    }

    MultiRecyclerViewAdapter(ArrayList<Model> dataSet, Context context)
    {
        this.dataSet = dataSet;
        this.context = context;
        this.total_types = dataSet.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view;

        if (viewType == Model.TEXT_TYPE)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_items,parent,false);
            return new TextTypeViewHolder(view);
        }
        else if (viewType == Model.IMAGE_TYPE)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_items,parent,false);
            return new ImageTypeViewHolder(view);
        }
        else if (viewType == Model.VIDEO_TYPE)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_items,parent,false);
            return new VideoTypeViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Model object = dataSet.get(position);

        if (object != null)
        {
            switch (object.type)
            {
                case Model.TEXT_TYPE:
                    ((TextTypeViewHolder) holder).textView.setText(object.text);
                    break;

                case Model.IMAGE_TYPE:
                    ((ImageTypeViewHolder) holder).imageView.setImageResource(object.data);
                    break;

                    case Model.VIDEO_TYPE:
                        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                        exoPlayer = ExoPlayerFactory.newSimpleInstance(context,trackSelector);

                        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(object.data));

                        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(context);

                        try
                        {
                            rawResourceDataSource.open(dataSpec);
                        }
                        catch (RawResourceDataSource.RawResourceDataSourceException e)
                        {
                            e.printStackTrace();
                        }

                        DataSource.Factory factory = new DataSource.Factory()
                        {
                            @Override
                            public DataSource createDataSource()
                            {
                                return rawResourceDataSource;
                            }
                        };

                        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                        MediaSource videoSource = new ExtractorMediaSource(rawResourceDataSource.getUri(),factory,extractorsFactory,null,null);

                        ((VideoTypeViewHolder) holder).simpleExoPlayerView.setPlayer(exoPlayer);
                        exoPlayer.prepare(videoSource);
                        exoPlayer.setPlayWhenReady(true);

                        break;
            }
        }
    }

    @Override
    public int getItemViewType(int position)
    {

        switch (dataSet.get(position).type)
        {
            case 0:
                return Model.TEXT_TYPE;
            case 1:
                return Model.IMAGE_TYPE;
            case 2:
                return Model.VIDEO_TYPE;
            default:
                return -1;
        }
    }

    @Override
    public int getItemCount()
    {
        return dataSet.size();
    }
}
