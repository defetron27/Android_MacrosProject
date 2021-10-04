package com.deffe.macros.grindersouls;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GrinderSoulsGlideModule extends AppGlideModule
{
    @Override
    public boolean isManifestParsingEnabled()
    {
        return false;
    }
}
