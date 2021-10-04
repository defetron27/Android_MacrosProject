package com.deffe.macros.macrobots;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ContactsActivity extends AppCompatActivity
{

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        listView = (ListView)findViewById(R.id.list_View);
    }

    public void get(View v)
    {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

        startManagingCursor(cursor);

        String[] from = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone._ID};

        int[] to = {android.R.id.text1,android.R.id.text2};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,cursor,from,to);

        listView.setAdapter(simpleCursorAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    }
}
