package com.deffe.macros.animations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

/**
 * Created by Deffe on 3/23/2018.
 */

public class TABSDefaultProcessor implements ViewProcessor
{
    public TABSDefaultProcessor()
    {
    }


    public void process(@NonNull Context context, @Nullable String key, @Nullable View view, @Nullable Void extra) {
        if(view != null && view.getTag() != null && view.getTag() instanceof String) {
            String tag = (String)view.getTag();
            if(tag.contains(",")) {
                String[] splitTag = tag.split(",");
                String[] arr$ = splitTag;
                int len$ = splitTag.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    String part = arr$[i$];
                    this.processTagPart(context, key, view, part);
                }
            } else {
                this.processTagPart(context, key, view, tag);
            }

        }
    }

    private void processTagPart(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String part) {
        int pipe = part.indexOf(124);
        if(pipe != -1) {
            String prefix = part.substring(0, pipe);
            String suffix = part.substring(pipe + 1);
            TagProcessor processor = ATE.getTagProcessor(prefix);
            if(processor != null) {
                if(!processor.isTypeSupported(view)) {
                    throw new IllegalStateException(String.format("A view of type %s cannot use %s tags.", new Object[]{view.getClass().getName(), prefix}));
                } else {
                    try {
                        processor.process(context, key, view, suffix);
                    } catch (Throwable var10) {
                        throw new RuntimeException(String.format("Failed to run %s: %s", new Object[]{processor.getClass().getName(), var10.getMessage()}), var10);
                    }
                }
            } else {
                throw new IllegalStateException("No ATE tag processors found by prefix " + prefix);
            }
        }
    }
}
