package com.deffe.macros.grindersouls;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;


import java.util.ArrayList;
import java.util.List;


public class CustomFilterFragment extends Fragment implements SeekBar.OnSeekBarChangeListener
{

    private CustomFilterFragmentListener listener;

    private SeekBar seekBarBrightness;

    private SeekBar seekBarContrast;

    private SeekBar seekBarSaturation;


    public CustomFilterFragment()
    {
        // Required empty public constructor
    }


    public void setListener(CustomFilterFragmentListener listener)
    {
        this.listener = listener;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_custom_filter, container, false);

        seekBarBrightness = view.findViewById(R.id.seekbar_brightness);
        seekBarContrast = view.findViewById(R.id.seekbar_contrast);
        seekBarSaturation = view.findViewById(R.id.seekbar_saturation);

        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);

        // keeping contrast value b/w 1.0 - 3.0
        seekBarContrast.setMax(20);
        seekBarContrast.setProgress(0);

        // keeping saturation value b/w 0.0 - 3.0
        seekBarSaturation.setMax(30);
        seekBarSaturation.setProgress(10);

        seekBarBrightness.setOnSeekBarChangeListener(this);
        seekBarContrast.setOnSeekBarChangeListener(this);
        seekBarSaturation.setOnSeekBarChangeListener(this);


        return view;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b)
    {
        if (listener != null)
        {
            if (seekBar.getId() == R.id.seekbar_brightness)
            {
                // brightness values are b/w -100 to +100
                listener.onBrightnessChanged(progress - 100);
            }

            if (seekBar.getId() == R.id.seekbar_contrast)
            {
                // converting int value to float
                // contrast values are b/w 1.0f - 3.0f
                // progress = progress > 10 ? progress : 10;
                progress += 10;
                float floatVal = .10f * progress;
                listener.onContrastChanged(floatVal);
            }

            if (seekBar.getId() == R.id.seekbar_saturation)
            {
                // converting int value to float
                // saturation values are b/w 0.0f - 3.0f
                float floatVal = .10f * progress;
                listener.onSaturationChanged(floatVal);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        if (listener != null)
        {
            listener.onEditStarted();
        }

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        if (listener != null)
        {
            listener.onEditCompleted();
        }

    }

    public void resetControls() {
        seekBarBrightness.setProgress(100);
        seekBarContrast.setProgress(0);
        seekBarSaturation.setProgress(10);
    }

    public interface CustomFilterFragmentListener
    {
        void onBrightnessChanged(int brightness);

        void onSaturationChanged(float saturation);

        void onContrastChanged(float contrast);

        void onEditStarted();

        void onEditCompleted();
    }
}