package wibicom.wibeacon3;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Olivier on 6/20/2016.
 */
public class FragmentSettingIBeacon extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting_ibeacon, container, false);
    }

    public String getUUID()
    {
        TextView text = (TextView) getView().findViewById(R.id.editText_uuid);
        return text.getText().toString();
    }
    public String getMinor()
    {
        TextView text = (TextView) getView().findViewById(R.id.editText_minor);
        return text.getText().toString();
    }
    public String getMajor()
    {
        TextView text = (TextView) getView().findViewById(R.id.editText_major);
        return text.getText().toString();
    }

}
