package com.deffe.macros.soulsspot;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment implements ISearch
{

    private static final String ARG_SEARCHTERM = "search_term";
    private String mSearchTerm = null;

    ArrayList<String> strings = null;
    private IFragmentListener mIFragmentListener = null;
    ArrayAdapter<String> arrayAdapter = null;




    public StatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v;

        v = inflater.inflate(R.layout.fragment_status, container, false);

        ListView listView = (ListView) v.findViewById(R.id.listview1);
        strings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            strings.add(String.valueOf(i));
        }
        strings.add("11");
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(arrayAdapter);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getDataFromFragment_one(strings);
        if (getArguments() != null) {
            mSearchTerm = (String) getArguments().get(ARG_SEARCHTERM);
        }

        return v;
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

    @Override
    public void onTextQuery(String text)
    {

    }

    public static StatusFragment newInstance(String searchTerm)
    {
        StatusFragment fragment = new StatusFragment();
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
        mIFragmentListener.addiSearch(StatusFragment.this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (null != mIFragmentListener)
            mIFragmentListener.removeISearch(StatusFragment.this);
    }
}
