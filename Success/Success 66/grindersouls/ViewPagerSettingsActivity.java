package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ViewPagerSettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_settings);

        createRadioButtons();

        String savedTransformer = getTransformers(this);
        Toast.makeText(this, savedTransformer, Toast.LENGTH_SHORT).show();
    }


    private void createRadioButtons()
    {
        RadioGroup group = findViewById(R.id.radio_group_transformers_list);

        String[] transformers = getResources().getStringArray(R.array.pagertransformer_list);

        for (final String transformer : transformers)
        {
            RadioButton button = new RadioButton(this);
            button.setText(transformer);

            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(ViewPagerSettingsActivity.this, "You Clicked " + transformer, Toast.LENGTH_SHORT).show();

                    saveTransformers(transformer);
                }
            });

            group.addView(button);

            if (transformer.equals(getTransformers(this)))
            {
                button.setChecked(true);
            }
        }
    }

    private void saveTransformers(String transformer)
    {
        SharedPreferences preferences = this.getSharedPreferences("Transformers",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("transformer",transformer);

        editor.apply();
    }

    public static String getTransformers(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences("Transformers",MODE_PRIVATE);

        return  preferences.getString("transformer","DefaultTransformer");
    }
}
