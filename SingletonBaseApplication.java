package wibicom.wibeacon3;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by Olivier on 6/13/2016.
 */
public class SingletonBaseApplication extends Application {

    public static BluetoothDevice device;
    public static List<BluetoothGattService> services;
    public static BluetoothGatt bluetoothGatt;

    private static SingletonBaseApplication ourInstance = new SingletonBaseApplication();

    public static SingletonBaseApplication getInstance() {
        if(ourInstance == null)
            ourInstance = new SingletonBaseApplication();

        return ourInstance;
    }

    public static BluetoothDevice getDevice()
    {
        return device;
    }

    private SingletonBaseApplication() {
    }


}
