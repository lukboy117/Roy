package tw.roy.myutiles.ble;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;

import tw.roy.myutiles.R;
import tw.roy.myutiles.Utils;


public class BleFragment extends Fragment {

    private final String TAG = "BleFragment";
    private final int MY_BLUETOOTH_ENABLE_REQUEST_ID = 99;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 98;
    private BLE ble;
    private BluetoothAdapter mBluetoothAdapter;
    private OpenBluetoothCallBack openBluetoothCallBack;
    private RequestPermissionsResult requestPermissionsResult;
    private boolean checkGPS = false;

    public interface OpenBluetoothCallBack {
        void openBluetoothCallBack(boolean success);
    }

    public interface RequestPermissionsResult {
        void userDenied();

        void doAfterLocationSetting();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ble = BLE.getInstance(getActivity());
        if (ble != null) {
            mBluetoothAdapter = ble.getBluetoothAdapter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (checkGPS) {
            checkGPS = false;
            if (requestPermissionsResult != null) {
                requestPermissionsResult.doAfterLocationSetting();
            }
        }
    }

    public BLE getBle() {
        return ble;
    }

    public void openBluetooth(boolean shouldAsk) {
        openBluetooth(shouldAsk, null);
    }

    public void openBluetooth(boolean shouldAsk, OpenBluetoothCallBack openBluetoothCallBack) {

        boolean success = false;
        this.openBluetoothCallBack = openBluetoothCallBack;

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                if (shouldAsk) {

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, MY_BLUETOOTH_ENABLE_REQUEST_ID);

                    return;

                } else {
                    mBluetoothAdapter.enable();

                    success = true;
                }
            } else {

                success = true;
            }
        } else {

            Log.e(TAG, "BluetoothAdapter is null");
        }

        if (this.openBluetoothCallBack != null)
            this.openBluetoothCallBack.openBluetoothCallBack(success);
    }

    public void startScanWithLocationPermission(String UUID, RequestPermissionsResult requestPermissionsResult) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Utils.getTargetSdkVersion(getActivity()) >= Build.VERSION_CODES.M) {

            this.requestPermissionsResult = requestPermissionsResult;
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        } else {

            ble.startScan(UUID);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (!Utils.isGpsEnable(getActivity())) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                    ad.setTitle(R.string.open_bt_dialog_title);
                    ad.setMessage(R.string.open_bt_dialog_message);
                    ad.setPositiveButton(R.string.open_bt_dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkGPS = true;
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
                    ad.setNegativeButton(R.string.open_bt_dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (requestPermissionsResult != null) {
                                requestPermissionsResult.userDenied();
                            }
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();
                }

            } else {

                if (requestPermissionsResult != null) {
                    requestPermissionsResult.userDenied();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_BLUETOOTH_ENABLE_REQUEST_ID) {

            boolean success = false;

            if (resultCode == Activity.RESULT_OK) {
                success = true;
            }

            if (openBluetoothCallBack != null) {
                openBluetoothCallBack.openBluetoothCallBack(success);
            }
        }
    }
}
