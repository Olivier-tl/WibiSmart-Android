package wibicom.wibeacon3;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;


/**
 * Created by Olivier on 6/13/2016.
 */
public class BluetoothLeService extends Service {

    private static final String ADV_SERVICE_UUID = "00001520-1212-EFDE-1523-785FEABCD123";
    private static final String ADV_MODE_CHAR_UUID = "00003020-1212-efde-1523-785feabcd123";
    private static final String ADV_INTERVAL_CHAR_UUID = "00003021-1212-efde-1523-785feabcd123";
    private static final String ADV_POWER_CHAR_UUID = "00003022-1212-efde-1523-785feabcd123";
    private static final String ADV_SMART_CHAR_UUID = "00003023-1212-efde-1523-785feabcd123";
    private static final String ADV_PAYLOAD_CHAR_UUID = "00003024-1212-efde-1523-785feabcd123";

    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";

    public int beaconType = 0;
    public int txPower = 0;
    public int advertisingIntervals = 0;
    public int smartPowerMode = 0;

    public byte[] advertisingPayload;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    List<BluetoothGattService> services;

    Queue<BluetoothGattCharacteristic> characteristicQueue;
    List<BluetoothDevice> deviceList;
    List<String> deviceNameList;



    public int onStartCommand(Intent intent, int flags, int startId) {

        deviceList = new ArrayList<>();
        deviceNameList = new ArrayList<>();
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        return START_STICKY;
    }

    public void initialize() {

        if (mBluetoothManager == null)
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readAdvertisingCharacteristics() {
        BluetoothGattService advertisingService = mBluetoothGatt.getService(UUID.fromString(ADV_SERVICE_UUID));
        characteristicQueue = new LinkedList<>(advertisingService.getCharacteristics());
        if(!characteristicQueue.isEmpty())
            mBluetoothGatt.readCharacteristic(characteristicQueue.poll());
    }

    public void writeAdvertisingCharacteristics() {
        BluetoothGattService advertisingService = mBluetoothGatt.getService(UUID.fromString(ADV_SERVICE_UUID));
        characteristicQueue = new LinkedList<>(advertisingService.getCharacteristics());
        if(!characteristicQueue.isEmpty()) {
            writeAdvertisingCharacteristic(characteristicQueue.poll());
        }
    }

    private void writeAdvertisingCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (characteristic.getUuid().toString().equals(ADV_MODE_CHAR_UUID)) {
            characteristic.setValue(new byte[]{(byte)beaconType});
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if(characteristic.getUuid().toString().equals(ADV_INTERVAL_CHAR_UUID)) {
            characteristic.setValue(new byte[]{(byte)advertisingIntervals});
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if(characteristic.getUuid().toString().equals(ADV_POWER_CHAR_UUID)) {
            characteristic.setValue(new byte[]{(byte)txPower});
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if(characteristic.getUuid().toString().equals(ADV_SMART_CHAR_UUID)) {
            characteristic.setValue(new byte[]{(byte)smartPowerMode});
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if(characteristic.getUuid().toString().equals(ADV_PAYLOAD_CHAR_UUID)) {
            characteristic.setValue(advertisingPayload);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }



    public List<BluetoothGattService> getSupportedGattServices() {
        return mBluetoothGatt.getServices();
    }

    public void connect(final String address) {

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }

    public void disconnect() {
        mBluetoothGatt.disconnect();
    }

    public void discoverServices()
    {
        mBluetoothGatt.discoverServices();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_GATT_CONNECTED);
            else
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                services = mBluetoothGatt.getServices();
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(!characteristicQueue.isEmpty()) {
                    writeAdvertisingCharacteristic(characteristicQueue.poll());
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                byte byteArray[] = characteristic.getValue();

                // Set the right characteristic
                if (characteristic.getUuid().toString().equals(ADV_MODE_CHAR_UUID)) {
                    beaconType = ((int)byteArray[0]);
                }
                else if(characteristic.getUuid().toString().equals(ADV_INTERVAL_CHAR_UUID)) {
                    advertisingIntervals = ((int)byteArray[0]);
                }
                else if(characteristic.getUuid().toString().equals(ADV_POWER_CHAR_UUID)) {
                    txPower = ((int)byteArray[0]);
                }
                else if(characteristic.getUuid().toString().equals(ADV_SMART_CHAR_UUID)) {
                    smartPowerMode = ((int)byteArray[0]);
                }

                // Read the next characteristic in the queue & broadcast when queue is empty.
                if(characteristicQueue.isEmpty())
                    broadcastUpdate(ACTION_DATA_AVAILABLE);
                else
                {
                    mBluetoothGatt.readCharacteristic(characteristicQueue.poll());
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

        }
    };






}
