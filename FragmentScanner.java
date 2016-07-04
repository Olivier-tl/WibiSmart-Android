package wibicom.wibeacon3;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wibicom.wibeacon3.dummy.DummyContent;
import wibicom.wibeacon3.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FragmentScanner extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    List<BluetoothDevice> deviceList = new ArrayList<>();

    private RecyclerView recyclerView;
    private MyItemRecyclerViewAdapter myItemRecyclerViewAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentScanner() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FragmentScanner newInstance(int columnCount) {
        FragmentScanner fragment = new FragmentScanner();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public void updateList(BluetoothDevice newDevice)
    {
        if(!deviceList.contains(newDevice))
        {
            deviceList.add(newDevice);
            myItemRecyclerViewAdapter.insert(deviceList.size() - 1);
        }

        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    public void clearList()
    {
        int pos;
        while(!deviceList.isEmpty())
        {
            pos = deviceList.size() - 1;
            deviceList.remove(pos);
            myItemRecyclerViewAdapter.remove(pos);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set animation.
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setMoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);



        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        myItemRecyclerViewAdapter = new MyItemRecyclerViewAdapter(deviceList, mListener);
        final MyItemRecyclerViewAdapter adapter = myItemRecyclerViewAdapter;
        recyclerView.setAdapter(adapter);


        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearList();
                mListener.refreshScan();

            }
        });

        return view;
    }

    public void onConnect(BluetoothDevice device, int pos)
    {
        recyclerView.getLayoutManager().getChildAt(pos).findViewById(R.id.button_scan_item).setVisibility(View.VISIBLE);
        recyclerView.getLayoutManager().getChildAt(pos).setElevation(20f);
        deviceList.remove(pos);
        deviceList.add(0, device);
        myItemRecyclerViewAdapter.notifyItemMoved(pos, 0);

    }

    public void onDisconnect()
    {
        //recyclerView.getLayoutManager().getChildAt(pos).setElevation(20f);
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BluetoothDevice device, int position);
        void refreshScan();
    }


}
