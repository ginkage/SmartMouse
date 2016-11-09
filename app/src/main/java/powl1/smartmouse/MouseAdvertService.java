package powl1.smartmouse;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseData.Builder;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.UUID;

public class MouseAdvertService extends Service {
    private static final String TAG = MouseAdvertService.class.getSimpleName();
    private AdvertiseCallback mAdvertiseCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private final BroadcastReceiver mReceiver;
    private String mSavedName;

    private class MouseAdvertiseCallback extends AdvertiseCallback {
        private MouseAdvertiseCallback() {
        }

        public void onStartFailure(int errorCode) {
            Log.e(MouseAdvertService.TAG, "Unable to Start LE Advertisement");
            stopSelf();
        }

        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        }
    }

    public MouseAdvertService() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    if (state == 10 || state == 13) {
                        Log.e(MouseAdvertService.TAG, "Bluetooth disabled during advertising, stop");
                        stopSelf();
                    }
                }
            }
        };
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        registerReceiver(mReceiver, filter);
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
        Builder dataBuilder = new Builder();
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
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
            settingBuilder.setAdvertiseMode(1);
            settingBuilder.setTimeout(0);
            AdvertiseSettings settings = settingBuilder.build();
            settingBuilder.setConnectable(true);
            UUID hidServiceUUID = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");
            dataBuilder.setIncludeDeviceName(true);
            dataBuilder.setIncludeTxPowerLevel(true);
            dataBuilder.addServiceUuid(new ParcelUuid(hidServiceUUID));
            AdvertiseData data = dataBuilder.build();
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
