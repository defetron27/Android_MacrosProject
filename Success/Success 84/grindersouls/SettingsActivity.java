package com.deffe.macros.grindersouls;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class SettingsActivity extends BaseThemedActivity
{
    private ArrayList<String> settingsHeaderSection = new ArrayList<>();
    private ArrayList<String> settingsFooterSection = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView settingsListView = findViewById(R.id.settings_list_view);

        settingsHeaderSection.add("ViewPager Transformation Settings");
        settingsFooterSection.add("DefaultTransformation");

        settingsHeaderSection.add("Themes");
        settingsFooterSection.add("");

        SettingsAdapter settingsAdapter = new SettingsAdapter();

        settingsListView.setAdapter(settingsAdapter);
    }

    class SettingsAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return settingsHeaderSection.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View view = getLayoutInflater().inflate(R.layout.settings_content,null);

            final TextView settingsHeader = view.findViewById(R.id.settings_header);
            TextView settingsFooter = view.findViewById(R.id.settings_footer);

            String footer = settingsFooterSection.get(position);

            settingsHeader.setText(settingsHeaderSection.get(position));

            if (footer.equals(""))
            {
                settingsFooter.setVisibility(View.GONE);
            }
            else
            {
                settingsFooter.setText(settingsFooterSection.get(position));
            }

            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (settingsHeaderSection.get(position).equals("ViewPager Transformation Settings"))
                    {
                        startActivity(new Intent(SettingsActivity.this,ViewPagerSettingsActivity.class));
                    }
                    else
                    {
                        startActivity(new Intent(SettingsActivity.this,ThemeSettings.class));
                    }
                }
            });

            return view;
        }
    }

}
