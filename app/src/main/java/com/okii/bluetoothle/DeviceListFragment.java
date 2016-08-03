package com.okii.bluetoothle;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener{

    private static final String KEY_MAC_ADDRESS = "KEY_MAC_ADDRESS";
    private static final String[] KEYS = {"KEY_MAC_ADDRESS"};
    private static final int[] IDS = {android.R.id.text1};

    private AbsListView mListView;
    private TextView mEmptyView;

    private ListAdapter mAdapter;

    private String[] mDevices = null;

    public static DeviceListFragment newInstance(){
        return new DeviceListFragment();
    }

    public DeviceListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_device_list, container, false);

        if (view != null){
            mListView = (AbsListView)view.findViewById(android.R.id.list);
            mListView.setAdapter(mAdapter);

            mEmptyView = (TextView) view.findViewById(android.R.id.empty);
            mListView.setEmptyView(mEmptyView);

            mListView.setOnItemClickListener(this);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public void setEmptyText(CharSequence emptyText){
        if (mEmptyView != null){
            mEmptyView.setText(emptyText);
        }
    }

    public interface OnDeviceListFragmentInteractionListener{
        public void onDeviceListFragmentInteraction(String macAddress);
    }

    public void setScanning(boolean scanning){
        mListView.setEnabled(!scanning);
        setEmptyText(getString(scanning ? R.string.scanning : R.string.no_devices));
    }

}
