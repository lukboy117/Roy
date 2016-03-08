package tw.roy.myutiles.ble;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BLE {

    private final String TAG = "BLE";
    private String UUID;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;
    private ScanSettings settings;
    private Timer scanTimer;
    private boolean mScanning;

    private Map<String, DeviceData> devicesData;
    private long lastFindTime = -1; //最後一次搜到裝置的時間

    private List<OnBeaconSearchListener> onBeaconSearchListenerList;

    /**
     * 參數設定
     */
    private float scanPeriodSec = 2f; // second
    private int stopScanMinWithNotFound = -1; // minute (-1 = 不停止) 多久時間沒搜到裝置停止服務


    public interface OnBeaconSearchListener {
        void onBeaconSearch(List<Beacon> beaconList);
    }

    //Todo public
    public static BLE getInstance(Context context) {
        return getInstance(context, 2, -1);
    }

    public static BLE getInstance(Context context, float scanPeriodSec) {
        return getInstance(context, scanPeriodSec, -1);
    }

    public static BLE getInstance(Context context, int stopScanMinWithNotFound) {
        return getInstance(context, 2, stopScanMinWithNotFound);
    }

    public static BLE getInstance(Context context, float scanPeriodSec, int stopScanMinWithNotFound) {
        BLE ble = new BLE();
        ble.init(context, scanPeriodSec, stopScanMinWithNotFound);

        return new BLE();
    }

    public boolean isSupport() {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true;
        }
        Log.e(TAG, "Device doesn't support ble");
        return false;
    }

    public void addOnBeaconSearchListener(OnBeaconSearchListener onBeaconSearchListener) {
        if (onBeaconSearchListenerList == null) {
            onBeaconSearchListenerList = new ArrayList<>();
        }
        onBeaconSearchListenerList.add(onBeaconSearchListener);
    }

    public void setScanPeriodSec(float scanPeriodSec) {
        this.scanPeriodSec = scanPeriodSec;

        if(mScanning){
            startScan(UUID);
        }
    }

    public void setStopScanMinWithNotFound(int stopScanMinWithNotFound) {
        this.stopScanMinWithNotFound = stopScanMinWithNotFound;

        if(mScanning){
            startScan(UUID);
        }
    }

    public void stopScan() {

        if (scanTimer != null)
            scanTimer.cancel();
        scanLeDevice(false);
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void startScan(String UUID) {

        this.UUID = UUID;

        if (scanTimer != null)
            scanTimer.cancel();
        scanTimer = new Timer();
        scanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (stopScanMinWithNotFound != -1 && lastFindTime != -1) {
                    if (System.currentTimeMillis() - lastFindTime > stopScanMinWithNotFound * 60 * 1000) {
                        stopScan();
                    }
                }

                if (mBluetoothAdapter.isEnabled()) {

                    scanLeDevice(false);

                    try {

                        final List<Beacon> beaconList = new ArrayList<>();

                        for (Map.Entry<String, DeviceData> entry : devicesData.entrySet()) {

                            String key = entry.getKey();
                            DeviceData value = entry.getValue();

                            float temp = 0;

                            List<Integer> mLeDevicesRssiList = value.mLeDevicesRssiList;

                            if (mLeDevicesRssiList != null && mLeDevicesRssiList.size() > 0) {

                                for (int j = 0; j < mLeDevicesRssiList.size(); j++) {
                                    temp += mLeDevicesRssiList.get(j);
                                }

                                DecimalFormat df = new DecimalFormat("0.00");
                                final float aveRssi = Float.parseFloat(df.format(temp / (mLeDevicesRssiList.size())));


                                Beacon beacon = new Beacon();
                                beacon.setUUID(value.UUID);
                                beacon.setMajorID(value.Major);
                                beacon.setMinorID(value.Minor);
                                beacon.setRssi(aveRssi);

                                beaconList.add(beacon);

                            }
                        }

                        Collections.sort(beaconList, new mSort());

                        Log.d(TAG, "beaconList size= " + beaconList.size());

                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (onBeaconSearchListenerList != null) {
                                    for (OnBeaconSearchListener listener : onBeaconSearchListenerList) {

                                        listener.onBeaconSearch(beaconList);
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Calculate AvgRSSI ERROR");
                        e.printStackTrace();
                    }

                    devicesData.clear();

                    scanLeDevice(true);
                }
            }
        }, 0, (long) (scanPeriodSec * 1000));
    }

    public float rssi_2_dist(float rssi) {
        float A = 49;
        float n = (float) 4.5;
        double distance = Math.pow(10,
                ((Math.abs(rssi) - A) / (10 * n)));
        DecimalFormat df = new DecimalFormat("0.00");
        return Float.parseFloat(df.format(distance));
    }

    //Todo private
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void init(Context context, float scanPeriodSec, int stopScanMinWithNotFound) {

        if (context == null) {
            throw new NullPointerException("The context is null");
        }

        this.mContext = context;
        this.scanPeriodSec = scanPeriodSec;
        this.stopScanMinWithNotFound = stopScanMinWithNotFound;


        /**
         * Use this check to determine whether BLE is supported on the device.
         * Then you can selectively disable BLE-related features.
         */
        if (isSupport()) {

            if (Build.VERSION.SDK_INT >= 21) {

                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

                mScanCallback = new ScanCallback() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {

                        BluetoothDevice btDevice = result.getDevice();

                        // Get Scan Record byte array (Be warned, this can be null)
                        if (result.getScanRecord() != null) {

                            int rssi = result.getRssi();

                            byte[] scanData = result.getScanRecord().getBytes();

                            BleParser bleParser = BleParser.parse(scanData);

                            collectBleData(btDevice, bleParser, rssi);
                        }
                    }

//                    @SuppressLint("NewApi")
//                    @Override
//                    public void onBatchScanResults(List<ScanResult> results) {
//                        for (ScanResult sr : results) {
////                            MyLog.i("ScanResult - Results", sr.toString());
//                        }
//                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.e("Scan Failed", "Error Code: " + errorCode);
                    }
                };
            } else {
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

                    @Override
                    public void onLeScan(final BluetoothDevice device, final int rssi,
                                         final byte[] scanData) {

                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                BleParser bleParser = BleParser.parse(scanData);

                                collectBleData(device, bleParser, rssi);

                            }
                        }).start();

                    }
                };
            }


            /**
             * Initializes a Bluetooth adapter. For API level 18 and above, get a
             * reference to BluetoothAdapter through BluetoothManager.
             */
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            /**
             * Checks if Bluetooth is supported on the device.
             */
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Device doesn't support bluetooth");
            }

            devicesData = new HashMap<>();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(boolean enable) {

        if (mBluetoothAdapter == null) {

            Log.e(TAG, "The BluetoothAdapter is null");

            return;
        } else if (!mBluetoothAdapter.isEnabled()) {

            Log.e(TAG, "The BluetoothAdapter is not enabled");

            return;
        }


        if (enable && !mScanning) {
            mScanning = true;
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mBluetoothAdapter.getBluetoothLeScanner().startScan(null, settings, mScanCallback);
            }

            Log.d(TAG, "start scan");
        } else if (!enable && mScanning) {
            mScanning = false;
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }

            Log.d(TAG, "stop scan");
        }
    }

    private class mSort implements Comparator<Beacon> {
        public int compare(Beacon aveRssi1, Beacon aveRssi2) {

            float weighted_1 = aveRssi1.getRssi();
            float weighted_2 = aveRssi2.getRssi();

            return (weighted_1 < weighted_2) ? 1 : (weighted_1 == weighted_2) ? 0 : -1;
        }
    }

    private void collectBleData(BluetoothDevice mLeDevice, BleParser bleParser, int rssi) {

        String uuid = bleParser.uuid;
        int major = bleParser.major;
        int minor = bleParser.minor;

        lastFindTime = System.currentTimeMillis();

        String key = major + "_" + minor;

        if (this.UUID.equals(uuid)) {
            if ((devicesData.size() == 0) || !devicesData.containsKey(key)) {

                DeviceData deviceData = new DeviceData();
                deviceData.mLeDevices = mLeDevice;
                deviceData.Major = major;
                deviceData.Minor = minor;
                deviceData.UUID = uuid;
                deviceData.Rssi = rssi;

                /**
                 * Give new device a distance list.
                 */
                List<Integer> list = new ArrayList<>();
                list.add(rssi);

                deviceData.mLeDevicesRssiList = list;

                devicesData.put(key, deviceData);

            }
            //已搜過裝置
            else {

                /**
                 * Add device's distance to list.
                 */
                if (devicesData.get(key) != null) {
                    List<Integer> list = devicesData.get(key).mLeDevicesRssiList;
                    if (list != null) {
                        list.add(rssi);
                    }
                }
            }
        }
    }
}
