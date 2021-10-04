package com.deffe.macros.imageprocess;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

public class StickerBSFragment extends BottomSheetDialogFragment {

    public StickerBSFragment() {
        // Required empty public constructor
    }

    private StickerListener mStickerListener;

    private ArrayList<Integer> stickerList = new ArrayList<>();

    public void setStickerListener(StickerListener stickerListener) {
        mStickerListener = stickerListener;
    }

    public interface StickerListener {
        void onStickerClick(Bitmap bitmap);
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };


    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sticker_emoji_dialog, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        RecyclerView rvEmoji = contentView.findViewById(R.id.rvEmoji);

        stickerList.clear();

        stickerList.add(R.drawable.aa);
        stickerList.add(R.drawable.bb);
        stickerList.add(R.drawable.s);
        stickerList.add(R.drawable.t);
        stickerList.add(R.drawable.u);
        stickerList.add(R.drawable.v);



        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rvEmoji.setLayoutManager(gridLayoutManager);
        StickerAdapter stickerAdapter = new StickerAdapter(stickerList,getContext());
        rvEmoji.setAdapter(stickerAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder>
    {
        ArrayList<Integer> stickerList;
        Context context;

        StickerAdapter(ArrayList<Integer> stickerList, Context context)
        {
            this.stickerList = stickerList;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_sticker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            final int sticker = stickerList.get(position);

            holder.imgSticker.setImageResource(sticker);

            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mStickerListener != null)
                    {
                        mStickerListener.onStickerClick(BitmapFactory.decodeResource(getResources(), sticker));
                    }
                    dismiss();
                }
            });


        }

        @Override
        public int getItemCount()
        {
            return stickerList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            ImageView imgSticker;

            ViewHolder(View itemView)
            {
                super(itemView);
                imgSticker = itemView.findViewById(R.id.imgSticker);
            }
        }
    }

    private String convertEmoji(String emoji)
    {
        String returnedEmoji = "";
        try
        {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = getEmojiByUnicode(convertEmojiToInt);
        }
        catch (NumberFormatException e)
        {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }

    private String getEmojiByUnicode(int unicode)
    {
        return new String(Character.toChars(unicode));
    }
}