package com.deffe.macros.grindersouls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.deffe.macros.grindersouls.imageprocessors.Filter;

import java.util.List;


public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.ThumbnailsViewHolder>
{
    private static final String TAG = "THUMBNAILS_ADAPTER";

    private List<ThumbnailItem> dataSet;
    private ThumbnailsAdapterListener listener;
    private Context mContext;

    ThumbnailsAdapter(Context mContext,List<ThumbnailItem> dataSet,ThumbnailsAdapterListener listener)
    {
        Log.v(TAG, "Thumbnails Adapter has " + dataSet.size() + " items");
        this.mContext = mContext;
        this.dataSet = dataSet;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThumbnailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Log.v(TAG, "On Create View Holder Called");
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_thumbnail_item, parent, false);
        return new ThumbnailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailsViewHolder holder, @SuppressLint("RecyclerView") final int position)
    {
        final ThumbnailItem thumbnailItem = dataSet.get(position);
        Log.v(TAG, "On Bind View Called");


        holder.thumbnail.setImageBitmap(thumbnailItem.image);

        holder.thumbnail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                listener.onFilterSelected(thumbnailItem.filter);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return dataSet.size();

    }


    class ThumbnailsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView thumbnail;

        ThumbnailsViewHolder(View v)
        {
            super(v);
            thumbnail = v.findViewById(R.id.thumbnail);
        }
    }
    public interface ThumbnailsAdapterListener
    {
        void onFilterSelected(Filter filter);
    }
}
