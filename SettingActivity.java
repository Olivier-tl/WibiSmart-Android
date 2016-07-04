package wibicom.wibeacon3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


import java.util.List;
import java.util.Queue;

public class SettingActivity extends AppCompatActivity {

    private static final String ADV_SERVICE_UUID = "00001520-1212-EFDE-1523-785FEABCD123";
    private static final String ADV_MODE_CHAR_UUID = "00003020-1212-efde-1523-785feabcd123";
    private static final String ADV_INTERVAL_CHAR_UUID = "00003021-1212-efde-1523-785feabcd123";
    private static final String ADV_POWER_CHAR_UUID = "00003022-1212-efde-1523-785feabcd123";
    private static final String ADV_SMART_CHAR_UUID = "00003023-1212-efde-1523-785feabcd123";


    private BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    List<BluetoothGattService> services;
    Queue<BluetoothGattCharacteristic> characteristicsQueue;

    private BluetoothLeService mBluetoothLeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        FragmentSettingIBeacon firstFragment = new FragmentSettingIBeacon();

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.initialize();

            //mBluetoothLeService.readAdvertisingCharacteristics();

        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };



    private void loadSpinners()
    {
        // Beacon type spinner.
        final Spinner spinnerBeaconType = (Spinner) findViewById(R.id.spinner_beacon_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterBeaconType = ArrayAdapter.createFromResource(this,
                R.array.beacon_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBeaconType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBeaconType.setAdapter(adapterBeaconType);
        spinnerBeaconType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                int beaconType = spinnerBeaconType.getSelectedItemPosition();

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

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
        Spinner spinnerTxPower = (Spinner) findViewById(R.id.spinner_tx_power);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTxPower = ArrayAdapter.createFromResource(this,
                R.array.tx_power, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTxPower.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTxPower.setAdapter(adapterTxPower);


    }

    public void update(View view)
    {
        setTxPower();
        setAdvIntervals();
        setSmartPower();
        setBeaconType();

        setPayload();

        mBluetoothLeService.writeAdvertisingCharacteristics();
       // mBluetoothLeService.disconnect();
    }

    private void loadBeaconType(int beaconType)
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_beacon_type);
        spinner.setSelection(beaconType);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    }

    private void setPayload()
    {
        switch(mBluetoothLeService.beaconType)
        {
            case 0: setIBeaconPayload();
                break;
            case 1: setEddystoneUidPayload();
                break;
            case 2: setEddystoneUrlPayload();
                break;
            case 3:
                break;
            case 4:
                break;
            default:
                break;
        }
    }

    private void loadAdvIntervals(int intervals)
    {
        TextView text = (TextView) findViewById(R.id.intervals);
        text.setText(Integer.toString(intervals));
    }

    private void loadTxPower(int txPower)
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_tx_power);
        spinner.setSelection(txPower);
    }

    private void loadSmartPower(int isSmartPowerMode)
    {
        Switch switchSmartPower = (Switch) findViewById(R.id.switch_smart_power);
        switchSmartPower.setChecked(isSmartPowerMode != 0);
    }

    private void setBeaconType()
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_beacon_type);
        spinner.getSelectedItemPosition();
        mBluetoothLeService.beaconType = spinner.getSelectedItemPosition();
    }

    private void setAdvIntervals()
    {
        TextView text = (TextView) SettingActivity.this.findViewById(R.id.intervals);
        int advIntervals = Integer.parseInt(text.getText().toString(), 16);
        if(advIntervals > 0 && advIntervals <= 10240)
            mBluetoothLeService.advertisingIntervals = advIntervals;
    }

    private void setTxPower()
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_tx_power);
        spinner.getSelectedItemPosition();
        mBluetoothLeService.txPower = spinner.getSelectedItemPosition();

    }

    private void setSmartPower()
    {
        Switch switchSmartPower = (Switch) findViewById(R.id.switch_smart_power);
        mBluetoothLeService.smartPowerMode = switchSmartPower.isChecked() ? 1 : 0;
    }

    private void setIBeaconPayload()
    {
        FragmentSettingIBeacon fragIBeacon = (FragmentSettingIBeacon)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        String payload = fragIBeacon.getUUID() + fragIBeacon.getMajor() + fragIBeacon.getMinor();
        mBluetoothLeService.advertisingPayload = stringToBytes(payload);
    }

    private void setEddystoneUidPayload()
    {
        FragmentSettingEddyUid fragEddystoneUid = (FragmentSettingEddyUid)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        String payload = fragEddystoneUid.getNamespaceId() + fragEddystoneUid.getInstanceId();
        mBluetoothLeService.advertisingPayload = stringToBytes(payload);
    }

    private void setEddystoneUrlPayload()
    {
        FragmentSettingEddyUrl fragEddystoneUid = (FragmentSettingEddyUrl)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        String url = fragEddystoneUid.getURL();
        byte[] byteArray = new byte[url.length() + 1];
        byte[] payloadArray = url.getBytes();
        // Assign the URL bytes to the payload
        for(int i = 0; i < url.length(); i++)
        {
            byteArray[i] = payloadArray[i];
        }
        // Add the extension byte at the end of the payload
        byteArray[url.length()] = (byte)fragEddystoneUid.getExtention();

        mBluetoothLeService.advertisingPayload = byteArray;//stringToBytes(payload);
    }

    private byte[] stringToBytes(String payload)
    {
        byte[] byteArray = new byte[20];
        int j = 0;
        for(int i = 0; i < payload.length(); i++)
        {
            byteArray[j] = (byte)Integer.parseInt(payload.substring(i, i+2), 16);
            j++;
            i++;
        }
        return byteArray;
    }

    private void logToDisplay(final String line, final int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView) SettingActivity.this.findViewById(id);
                textView.setText(line);
            }
        });
    }


    public void switchToInfo(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                loadBeaconType(mBluetoothLeService.beaconType);
                loadAdvIntervals(mBluetoothLeService.advertisingIntervals);
                loadTxPower(mBluetoothLeService.txPower);
                loadSmartPower(mBluetoothLeService.smartPowerMode);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


}
