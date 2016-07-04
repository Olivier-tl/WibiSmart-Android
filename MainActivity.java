package wibicom.wibeacon3;

import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;


import java.lang.String;
import java.util.List;

import android.os.RemoteException;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.altbeacon.beacon.*;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;


public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, FragmentScanner.OnListFragmentInteractionListener{//, FragmentDashboard.OnFragmentInteractionListener {
    private BeaconManager beaconManager;
    private DrawerLayout mDrawerLayout;
    Collection<Beacon> beaconsInRange;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;

    List<BluetoothGattService> services;

    private boolean isScanStarted = false;
    private boolean isConnected = false;

    FragmentScanner fragmentScanner;
    FragmentDashboard fragmentDashboard;
    FragmentSettings fragmentSettings;

    BluetoothDevice connectedDevice;
    int connectedDevicePosition;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);

        setupUi();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //startService(gattServiceIntent);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setupUi()
    {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24px);
        ab.setDisplayHomeAsUpEnabled(true);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        fragmentScanner = new FragmentScanner();
        fragmentDashboard = new FragmentDashboard();
        fragmentSettings = new FragmentSettings();

        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(fragmentScanner, "Scanner");
        adapter.addFragment(fragmentDashboard, "Dashboard");
        adapter.addFragment(fragmentSettings, "Settings");
        viewPager.setAdapter(adapter);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                startScan();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.initialize();
            startScan();

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

            fragmentScanner.updateList(device);
        }
    };

    public void startScan()//View view)
    {
        if(!isScanStarted)
        {
            mBluetoothAdapter.startLeScan(leScanCallback);
            isScanStarted = true;
        }
        else
        {
            mBluetoothAdapter.stopLeScan(leScanCallback);
            mBluetoothAdapter.startLeScan(leScanCallback);
            //isScanStarted = false;
        }
    }

    public void disconnectDevice(View view)
    {
        if(isConnected)
        {
            mBluetoothLeService.disconnect();
            view.findViewById(R.id.button_scan_item).setVisibility(View.INVISIBLE);
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                mBluetoothLeService.discoverServices();
                isConnected = true;
                Snackbar.make(findViewById(android.R.id.content), "Connected!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                fragmentScanner.onConnect(connectedDevice, connectedDevicePosition);
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                Snackbar.make(findViewById(android.R.id.content), "Disconnected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                //logToDisplay("Connected!", R.id.connection);
                //services = mBluetoothLeService.services;
                //final List<String> servicesNames = new ArrayList<>();

                //logToDisplay("Service: " , R.id.info);

               // for (BluetoothGattService service : services) {
               //     servicesNames.add(service.getUuid().toString());
               // }

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view, servicesNames.toArray());
//                        ListView listView = (ListView) findViewById(R.id.listView2);
//                        listView.setAdapter(adapter);
//
//                    }
//                });
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

    public void onResume() {
        super.onResume();
//        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
//        // Detect the main identifier (UID) frame:
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
//        // Detect the telemetry (TLM) frame:
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("x,s:0-1=feaa,m:2-2=00,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
//        // Detect the URL frame:
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));
//        // Detect iBeacon format
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
//        // Detect some format
//        beaconManager.getBeaconParsers().add(new BeaconParser().
//                setBeaconLayout("m:0-1=A700,i:4-19,i:20-21,i:22-23,p:24-24"));
//
//        beaconManager.bind(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

//        beaconsInRange = beacons;
//        String beaconName = "Unknown";
//
//        for (final Beacon oneBeacon : beaconsInRange) {
//            if (oneBeacon.getServiceUuid() == 0xfeaa) {
//
//                switch (oneBeacon.getBeaconTypeCode()) {
//                    case 0x00:
//                        logToDisplay("Beacon type: Eddystone UID", R.id.beaconType);
//                        logToDisplay("Namespace ID: " + oneBeacon.getId1() + "\nInstance ID: " + oneBeacon.getId2(), R.id.info);
//                        beaconName = "Eddystone UID";
//                        break;
//                    case 0x10:
//                        logToDisplay("Beacon type: Eddystone URL", R.id.beaconType);
//                        String url = UrlBeaconUrlCompressor.uncompress(oneBeacon.getId1().toByteArray());
//                        logToDisplay("URL: " + url, R.id.info);
//                        beaconName = "Eddystone URL";
//                        break;
//                    case 0x20:
//                        // logToDisplay("Beacon type: Eddystone TLM", R.id.beaconType);
//                        beaconName = "Eddystone TLM";
//                        break;
//                    default: //logToDisplay("Beacon type: Bad Eddystone", R.id.beaconType);
//                        break;
//                }
//            }
//            else if (oneBeacon.getManufacturer() == 0x004c) {
//                logToDisplay("Beacon type: Apple iBeacon", R.id.beaconType);
//                logToDisplay("UUID: " + oneBeacon.getId1() + " \nMajor: " + oneBeacon.getId2() +  " \nMinor: " + oneBeacon.getId3(), R.id.info);
//                beaconName = "iBeacon";
//            }
//            else if(oneBeacon.getManufacturer() == 0x00A7)
//            {
//                logToDisplay("Beacon type: Wibeacon (light)", R.id.beaconType);
//
//
//
//                byte lightData[] = oneBeacon.getId1().toByteArray();
//
//                int vBat = (((lightData[1] & 0xFF) << 8) | lightData[0] & 0xFF);
//                double vbatMv = (vBat - 16.626)/274.25;
//                int vSolar = ((lightData[3] & 0xFF) << 8) | (lightData[2] & 0xFF);
//
//                logToDisplay("UUID: " + oneBeacon.getId1() + "\n\nVbat: " + vbatMv + "\nVsolar: " + vSolar, R.id.info);
//            }
//
//            if (oneBeacon.getExtraDataFields().size() > 0) {
//                long telemetryVersion = oneBeacon.getExtraDataFields().get(0);
//                long batteryMilliVolts = oneBeacon.getExtraDataFields().get(1);
//                long temp = oneBeacon.getExtraDataFields().get(2);
//                long pduCount = oneBeacon.getExtraDataFields().get(3);
//                long uptime = oneBeacon.getExtraDataFields().get(4);
//                // logToDisplay("Distance: " + temp + " deg", R.id.temp);
//            }
//
//            logToDisplay("Distance: " + oneBeacon.getDistance() + " m", R.id.distance);
//            sendNotification(beaconName);
//
//        }
    }

    @Override
    public void onBeaconServiceConnect() {
//        Identifier myBeaconNamespaceId = null;//Identifier.parse("0x2f234454f4911ba9ffa6");
//        Identifier myBeaconInstanceId = null; //Identifier.parse("0x000000000001");
//        final Region region = new Region("all-beacons-region", myBeaconNamespaceId, myBeaconInstanceId, null);//Identifier.parse("00EEAAAABBBBCCCCDDDDEEEE0102030405060000"), null, null);
//
//        beaconManager.setRangeNotifier(this);
//        try {
//            beaconManager.startRangingBeaconsInRegion(region);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }


    private void logToDisplay(final String line, final int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView) MainActivity.this.findViewById(id);
                textView.setText(line);
            }
        });
    }

//    public void displayBeacons(View view) {
//        logToDisplay("Beacon type: ", R.id.beaconType);
//        logToDisplay("Info: ", R.id.info);
//        logToDisplay("Distance: ", R.id.distance);
//
//    }
//
//    private void sendNotification(String notificationContent)
//    {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle("Beacon is near")
//                        .setContentText(notificationContent);
//        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, MainActivity.class);
//
//        // The stack builder object will contain an artificial back stack for the
//        // started Activity.
//        // This ensures that navigating backward from the Activity leads out of
//        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        int mId = 0;
//        // mId allows you to update the notification later on.
//        mNotificationManager.notify(mId, mBuilder.build());
//
//    }
//
//    public void switchToSettings(View view) {
//        Intent intent = new Intent(this, SettingActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//    public void switchToMenu(View view) {
//        Intent intent = new Intent(this, MenuActivity.class);
//        startActivity(intent);
//        finish();
//    }

    @Override
    public void onListFragmentInteraction(BluetoothDevice device, int position)
    {
        //BluetoothDevice bluetoothDevice = deviceList.get(pos);
        if(!isConnected)
        {
            Snackbar.make(findViewById(android.R.id.content), "Connecting to " + device.getName() + "...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            mBluetoothAdapter.stopLeScan(leScanCallback);
            mBluetoothLeService.connect(device.getAddress());

            connectedDevice = device;
            connectedDevicePosition = position;
        }


    }

    @Override
    public void refreshScan()
    {
        startScan();
    }


//    @Override
//    public void onFragmentInteraction()
//    {
//
//    }


    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

}

