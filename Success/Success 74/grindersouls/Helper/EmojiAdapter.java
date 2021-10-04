package com.deffe.macros.grindersouls.Helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.deffe.macros.grindersouls.R;
import com.deffe.macros.grindersouls.emoji.Emojicon;

import java.util.List;


class EmojiAdapter extends ArrayAdapter<Emojicon> {
    private boolean mUseSystemDefault = false;
    EmojiconGridView.OnEmojiconClickedListener emojiClickListener;


    public EmojiAdapter(Context context, List<Emojicon> data, boolean useSystemDefault) {
        super(context, R.layout.emojicon_item, data);
        mUseSystemDefault = useSystemDefault;
    }

    public EmojiAdapter(Context context, Emojicon[] data, boolean useSystemDefault) {
        super(context, R.layout.emojicon_item, data);
        mUseSystemDefault = useSystemDefault;
    }


    public void setEmojiClickListener(EmojiconGridView.OnEmojiconClickedListener listener){
        this.emojiClickListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = View.inflate(getContext(), R.layout.emojicon_item, null);
            ViewHolder holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.emojicon_icon);
            holder.icon.setUseSystemDefault(mUseSystemDefault);

            v.setTag(holder);
        }

         Emojicon emoji = getItem(position);
         ViewHolder holder = (ViewHolder) v.getTag();
             holder.icon.setText(emoji.getEmoji());
                holder.icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emojiClickListener.onEmojiconClicked(getItem(position));
                    }
                });

        return v;
    }

    class ViewHolder {
        EmojiconTextView icon;
    }
}