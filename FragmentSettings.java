package wibicom.wibeacon3;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSettings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSettings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSettings.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSettings newInstance(String param1, String param2) {
        FragmentSettings fragment = new FragmentSettings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        loadSpinners(view);
        return view;


    }

    private void loadSpinners(final View view)
    {
        // Beacon type spinner.
        final Spinner spinnerBeaconType = (Spinner) view.findViewById(R.id.spinner_beacon_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterBeaconType = ArrayAdapter.createFromResource(getContext(),
                R.array.beacon_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBeaconType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBeaconType.setAdapter(adapterBeaconType);
        spinnerBeaconType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                int beaconType = spinnerBeaconType.getSelectedItemPosition();

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

                switch(beaconType)
                {
                    case 0:
                        FragmentSettingIBeacon fragIBeacon = new FragmentSettingIBeacon();
                        transaction.replace(R.id.fragment_container, fragIBeacon);
                        break;
                    case 1:
                        FragmentSettingEddyUid fragEddyUid = new FragmentSettingEddyUid();
                        transaction.replace(R.id.fragment_container, fragEddyUid);
                        break;
                    case 2:
                        FragmentSettingEddyUrl fragEddyUrl = new FragmentSettingEddyUrl();
                        transaction.replace(R.id.fragment_container, fragEddyUrl);
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    default:
                        break;
                }

                transaction.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });

        // Tx power spinner.
        Spinner spinnerTxPower = (Spinner) view.findViewById(R.id.spinner_tx_power);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTxPower = ArrayAdapter.createFromResource(getContext(),
                R.array.tx_power, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTxPower.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTxPower.setAdapter(adapterTxPower);


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }
}
