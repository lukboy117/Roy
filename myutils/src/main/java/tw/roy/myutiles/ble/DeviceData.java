package tw.roy.myutiles.ble;

import android.bluetooth.BluetoothDevice;

import java.util.List;

class DeviceData {

    public BluetoothDevice mLeDevices;
    public int Major;
    public int Minor;
    public String UUID;
    public int Rssi;
    public List<Integer> mLeDevicesRssiList;

}
