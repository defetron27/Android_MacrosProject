package com.deffe.macros.soulsspot;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class GroupsFragment extends Fragment implements ISearch, IDataCallback
{

    private static final String ARG_SEARCHTERM = "search_term";
    private String mSearchTerm = null;

    ArrayList<String> strings = null;
    private IFragmentListener mIFragmentListener = null;
    ArrayAdapter<String> arrayAdapter = null;


    public GroupsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view;

        view = inflater.inflate(R.layout.fragment_groups, container, false);

        ListView listView = (ListView) view.findViewById(R.id.listview2);
        if (((MainActivity) getActivity()) != null)
        {
            ((MainActivity) getActivity()).setiDataCallback(this);
        }

        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, strings);

        listView.setAdapter(arrayAdapter);

        if (getArguments() != null)
        {
            mSearchTerm = (String) getArguments().get(ARG_SEARCHTERM);
        }

        return view;
    }

    @Override
    public void onTextQuery(String text)
    {
        arrayAdapter.getFilter().filter(text);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (null != mSearchTerm)
        {
            onTextQuery(mSearchTerm);
        }
    }

    public static GroupsFragment newInstance(String searchTerm)
    {
        GroupsFragment fragment = new GroupsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SEARCHTERM, searchTerm);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mIFragmentListener = (IFragmentListener) context;
        mIFragmentListener.addiSearch(GroupsFragment.this);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if (null != mIFragmentListener)
        {
            mIFragmentListener.removeISearch(GroupsFragment.this);
        }
    }

    @Override
    public void onFragmentCreated(ArrayList<String> listData)
    {
        strings = listData;
    }
}
