package powl1.smartmouse;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

public class MouseAdvertService extends Service {
    private static final String TAG = MouseAdvertService.class.getSimpleName();

    private AdvertiseCallback mAdvertiseCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private String mSavedName;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF
                        || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    Log.e(TAG, "Bluetooth disabled during advertising, stop");
                    stopSelf();
                }
            }
        }
    };

    private class MouseAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartFailure(int errorCode) {
            Log.e(TAG, "Unable to Start LE Advertisement");
            stopSelf();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to retrieve Bluetooth Adapter");
            stopSelf();
            return;
        }
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Log.e(TAG, "Unable to retrieve Bluetooth Advertiser");
            stopSelf();
        } else if (!startAdvertising()) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        stopAdvertising();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver");
        }
        super.onDestroy();
    }

    private boolean startAdvertising() {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "No Bluetooth Adapter");
            return false;
        } else if (mBluetoothLeAdvertiser == null) {
            Log.e(TAG, "No Bluetooth Advertiser");
            return false;
        } else if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Log.e(TAG, "BLE Multiple Advertisement Not Supported");
            return false;
        } else if (mAdvertiseCallback != null) {
            Log.e(TAG, "BLE advertising already ongoing");
            return true;
        } else {
            mSavedName = mBluetoothAdapter.getName();
            mBluetoothAdapter.setName("MyHID");
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setTimeout(0)
                    .setConnectable(true)
                    .build();
            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(true)
                    .addServiceUuid(new ParcelUuid(BluetoothGattHIDService.HIDServiceUUID))
                    .build();
            mAdvertiseCallback = new MouseAdvertiseCallback();
            mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
            return true;
        }
    }

    private void stopAdvertising() {
        if (mBluetoothAdapter != null) {
            if (mSavedName != null) {
                mBluetoothAdapter.setName(mSavedName);
            }
            if (mBluetoothLeAdvertiser == null) {
                Log.e(TAG, "No Bluetooth Advertiser");
            } else if (mAdvertiseCallback == null) {
                Log.e(TAG, "No Advertise Callback");
            } else if (mBluetoothAdapter.isEnabled()) {
                mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                mAdvertiseCallback = null;
            }
        }
    }
}
