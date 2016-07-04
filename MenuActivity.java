package wibicom.wibeacon3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wibicom.wibeacon3.FragmentScanner.OnListFragmentInteractionListener;

public class MenuActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    BluetoothGatt bluetoothGatt;
    List<BluetoothDevice> deviceList;
    List<String> deviceNameList;
    List<BluetoothGattService> services;
    //private SingBroadcastReceiver mReceiver;
    private boolean isScanStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        deviceList = new ArrayList<>();
        deviceNameList = new ArrayList<>();
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //startService(gattServiceIntent);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.initialize();

            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };



    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

           if(!deviceList.contains(device)) {
               deviceList.add(device);
               if (device.getName() != null)
                   deviceNameList.add(device.getName());
               else
                   deviceNameList.add("N/A");
           }

            ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view, deviceNameList.toArray());

            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,int position, long id) {

                    BluetoothDevice bluetoothDevice = deviceList.get(position);
                    //SingletonBaseApplication.device = bluetoothDevice;
                    mBluetoothAdapter.stopLeScan(leScanCallback);
                    mBluetoothLeService.connect(bluetoothDevice.getAddress());
                    //bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, btleGattCallback);
                    //SingletonBaseApplication.bluetoothGatt = bluetoothGatt;
                    logToDisplay("Connecting...", R.id.connection);
                    //bluetoothGatt.discoverServices();
                    TextView textView = (TextView) MenuActivity.this.findViewById(R.id.connection);
                    textView.setTextColor(0xFFFFEE00);
                }
            });
        }
    };

    private void logToDisplay(final String line, final int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView) MenuActivity.this.findViewById(id);
                textView.setText(line);
            }
        });
    }



    public void startScan(View view)
    {

        if(!isScanStarted)
        {
            mBluetoothAdapter.startLeScan(leScanCallback);
            isScanStarted = true;
            Button buttonScan = (Button)MenuActivity.this.findViewById(R.id.button_scan);
            buttonScan.setText("Stop Scan");
            logToDisplay("Information: " + "Scan started!", R.id.info);
        }
        else
        {
            mBluetoothAdapter.stopLeScan(leScanCallback);
            Button buttonScan = (Button)MenuActivity.this.findViewById(R.id.button_scan);
            isScanStarted = false;
            buttonScan.setText("Start Scan");
        }

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

                mBluetoothLeService.discoverServices();
                logToDisplay("Connected!\nDiscovering services...", R.id.connection);
                TextView textView = (TextView) MenuActivity.this.findViewById(R.id.connection);
                textView.setTextColor(0xFF00FF00);

            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                logToDisplay("Disconnected", R.id.connection);
                TextView textView = (TextView) MenuActivity.this.findViewById(R.id.connection);
                textView.setTextColor(0xFFFF0000);

            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                logToDisplay("Connected!", R.id.connection);
                services = mBluetoothLeService.services;
                final List<String> servicesNames = new ArrayList<>();

                logToDisplay("Service: " , R.id.info);

                for (BluetoothGattService service : services) {
                    servicesNames.add(service.getUuid().toString());
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view, servicesNames.toArray());
                        ListView listView = (ListView) findViewById(R.id.listView2);
                        listView.setAdapter(adapter);

                    }
                });
            }
        }
    };



    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }




}


