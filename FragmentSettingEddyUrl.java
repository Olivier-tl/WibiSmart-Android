package wibicom.wibeacon3;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Olivier on 6/20/2016.
 */
public class FragmentSettingEddyUrl extends Fragment {
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        loadSpinner();
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_eddystone_url, container, false);
        loadSpinner(view);
        return view;
  }

    public String getURL()
    {
        TextView text = (TextView) getView().findViewById(R.id.editText_url);
        return text.getText().toString();
    }
    public int getExtention()
    {
        Spinner spinner = (Spinner) getView().findViewById(R.id.spinner_url);
        return spinner.getSelectedItemPosition();
    }

    private void loadSpinner(View view)
    {
        final Spinner spinnerExtension = (Spinner) view.findViewById(R.id.spinner_url);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterExtensions = ArrayAdapter.createFromResource(getContext(),
                R.array.extensions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterExtensions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerExtension.setAdapter(adapterExtensions);
    }


}
