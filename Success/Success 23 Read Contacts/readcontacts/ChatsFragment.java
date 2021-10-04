package com.deffe.macros.readcontacts;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment
{
    private ListView contactsListView;



    public ChatsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        contactsListView = (ListView) view.findViewById(R.id.contacts_listview);

        getContacts();

        return view;
    }

    private void getContacts()
    {
        ArrayList<Contact_Items> contacts_ArrayList = new ArrayList<>();

        Cursor cursor_contacts = null;

        ContentResolver contentResolver = getActivity().getContentResolver();

        try
        {
            cursor_contacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        }
        catch (Exception e)
        {
            Log.e("Error on Contact",e.getMessage());
        }

        if (cursor_contacts.getCount() > 0)
        {
            while (cursor_contacts.moveToNext())
            {
                Contact_Items contact_items = new Contact_Items();

                String contact_id = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts._ID));

                int hasPhoneNumber = Integer.parseInt(cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0)
                {
                    Cursor phoneCursor = contentResolver.query
                            (
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{contact_id},
                                    null
                            );

                    while (phoneCursor.moveToNext())
                    {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        contact_items.contact_Number = phoneNumber;
                    }

                    phoneCursor.close();
                }

                contacts_ArrayList.add(contact_items);
            }

            ContactsAdapter adapter = new ContactsAdapter(getContext(),contacts_ArrayList);

            contactsListView.setAdapter(adapter);
        }
    }

    public class ContactsAdapter extends BaseAdapter
    {

        Context context;
        List<Contact_Items> contact_itemsList;

        public ContactsAdapter(Context context, List<Contact_Items> contact_itemsList)
        {
            this.context = context;
            this.contact_itemsList = contact_itemsList;
        }

        @Override
        public int getCount()
        {
            return contact_itemsList.size();
        }

        @Override
        public Object getItem(int i)
        {
            return contact_itemsList.get(i);
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup)
        {

            View view = View.inflate(context,R.layout.contacts_list_item,null);

            TextView TextViewMobileNumber = (TextView) view.findViewById(R.id.textView_mobile_number);

            TextViewMobileNumber.setText(contact_itemsList.get(i).contact_Number);

            return view;
        }
    }

    public class Contact_Items
    {
        public String contact_Number = "";
        public int contact_Id = 0;
    }






}
