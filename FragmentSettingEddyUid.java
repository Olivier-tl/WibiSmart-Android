package wibicom.wibeacon3;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Olivier on 6/21/2016.
 */
public class FragmentSettingEddyUid extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting_eddystone_uid, container, false);
    }

    public String getNamespaceId()
    {
        TextView text = (TextView) getView().findViewById(R.id.editText_namespace_id);
        return text.getText().toString();
    }
    public String getInstanceId()
    {
        TextView text = (TextView) getView().findViewById(R.id.editText_instance_id);
        return text.getText().toString();
    }
}
