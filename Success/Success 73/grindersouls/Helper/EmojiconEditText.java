package com.deffe.macros.grindersouls.Helper;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;

import com.deffe.macros.grindersouls.R;

public class EmojiconEditText extends AppCompatEditText
{
    private int mEmojiconSize;
    private int mEmojiconAlignment;
    private int mEmojiconTextSize;
    private boolean mUseSystemDefault = false;

    private String[] imgTypeString;

    public String[] getImgTypeString() {
        return imgTypeString;
    }

    public void setImgTypeString(String[] imgTypeString) {
        this.imgTypeString = imgTypeString;
    }

    private KeyBoardInputCallbackListener keyBoardInputCallbackListener;

    public interface KeyBoardInputCallbackListener
    {
        void onCommitContent(InputContentInfoCompat inputContentInfoCompat, int flags, Bundle opts);
    }

    public EmojiconEditText(Context context)
    {
        super(context);
        mEmojiconSize = (int) getTextSize();
        mEmojiconTextSize = (int) getTextSize();
        initView();
    }

    public EmojiconEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs);
        initView();
    }

    private void initView()
    {
        imgTypeString = new String[]{"image/png","image/gif","image/jpeg","image/webp"};
    }

    public EmojiconEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(attrs);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        final InputConnection inputConnection = super.onCreateInputConnection(outAttrs);

        EditorInfoCompat.setContentMimeTypes(outAttrs,imgTypeString);

        final InputConnectionCompat.OnCommitContentListener callback = new InputConnectionCompat.OnCommitContentListener()
        {
            @Override
            public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts)
            {
                if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0)
                {
                    try
                    {
                        inputContentInfo.requestPermission();
                    }
                    catch (Exception e)
                    {
                        return false;
                    }
                }

                boolean supported = false;

                for (final String mimeType : imgTypeString)
                {
                    if (inputContentInfo.getDescription().hasMimeType(mimeType))
                    {
                        supported = true;
                        break;
                    }
                }
                if (!supported)
                {
                    return false;
                }

                if (keyBoardInputCallbackListener != null)
                {
                    keyBoardInputCallbackListener.onCommitContent(inputContentInfo,flags,opts);
                }

                return true;
            }
        };
        return InputConnectionCompat.createWrapper(inputConnection,outAttrs,callback);
    }

    public void setKeyBoardInputCallbackListener(KeyBoardInputCallbackListener keyBoardInputCallbackListener)
    {
        this.keyBoardInputCallbackListener = keyBoardInputCallbackListener;
    }



    private void init(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        mEmojiconAlignment = a.getInt(R.styleable.Emojicon_emojiconAlignment, DynamicDrawableSpan.ALIGN_BASELINE);
        mUseSystemDefault = a.getBoolean(R.styleable.Emojicon_emojiconUseSystemDefault, false);
        a.recycle();
        mEmojiconTextSize = (int) getTextSize();
        setText(getText());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {
        updateText();
    }

    public void setEmojiconSize(int pixels)
    {
        mEmojiconSize = pixels;

        updateText();
    }

    private void updateText()
    {
        EmojiconHandler.addEmojis(getContext(), getText(), mEmojiconSize, mEmojiconAlignment, mEmojiconTextSize, mUseSystemDefault);
    }

    public void setUseSystemDefault(boolean useSystemDefault)
    {
        mUseSystemDefault = useSystemDefault;
    }
}
