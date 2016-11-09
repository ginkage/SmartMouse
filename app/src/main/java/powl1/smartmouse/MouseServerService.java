package powl1.smartmouse;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

public class MouseServerService extends Service {
    private static final boolean DBG = false;
    public static String STATE_CHANGED = "com.powl1.smartmouse.STATE_CHANGED";
    private static final String TAG = MouseServerService.class.getSimpleName();
    private BluetoothGattBatteryService mBatteryService;
    private final IBinder mBinder;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothDevice mConnectedDevice;
    private BluetoothGattDeviceInformationService mDevInfoService;
    private final BluetoothGattServerCallback mGattServerCallback;
    private BluetoothGattHIDService mHIDService;
    private double mSensitivity;

    public class MouseServerBinder extends Binder {
        MouseServerService getService() {
            return MouseServerService.this;
        }
    }

    public MouseServerService() {
        mSensitivity = 1.5d;
        mGattServerCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                if (newState == 2) {
                    mConnectedDevice = device;
                } else if (device == mConnectedDevice || newState == 0) {
                    mConnectedDevice = null;
                }
                sendStateChanged();
                super.onConnectionStateChange(device, status, newState);
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                byte[] value;
                if (offset != 0) {
                    value = Arrays.copyOfRange(characteristic.getValue(), offset, characteristic.getValue().length);
                } else {
                    value = characteristic.getValue();
                }
                mBluetoothGattServer.sendResponse(device, requestId, 0, offset, value);
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                characteristic.setValue(value);
                mBluetoothGattServer.sendResponse(device, requestId, 0, offset, null);
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                mBluetoothGattServer.sendResponse(device, requestId, 0, offset, descriptor.getValue());
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                descriptor.setValue(value);
                mBluetoothGattServer.sendResponse(device, requestId, 0, offset, null);
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                mBluetoothGattServer.sendResponse(device, requestId, 0, 0, null);
                super.onExecuteWrite(device, requestId, execute);
            }
        };
        mBinder = new MouseServerBinder();
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onCreate() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e(TAG, "Unable to retrieve Bluetooth Manager");
            return;
        }
        if (!mBluetoothManager.getAdapter().isEnabled()) {
            stopSelf();
        }
        startServer();
        super.onCreate();
    }

    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    private void startServer() {
        if (mBluetoothManager == null) {
            Log.e(TAG, "Unable to retrieve Bluetooth Manager");
        } else if (mBluetoothGattServer != null) {
            Log.e(TAG, "Gatt Server already open");
        } else {
            try {
                mBluetoothGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
            } catch (Exception e) {
                mBluetoothGattServer = null;
            }
            if (mBluetoothGattServer == null) {
                Log.e(TAG, "Unable to open Gatt Server");
                return;
            }
            mHIDService = new BluetoothGattHIDService();
            try {
                mBluetoothGattServer.addService(mHIDService);
            } catch (Exception e2) {
                Log.e(TAG, "Unable to add HID Service");
            }
            mBatteryService = new BluetoothGattBatteryService();
            try {
                mBluetoothGattServer.addService(mBatteryService);
            } catch (Exception e3) {
                Log.e(TAG, "Unable to add Battery Service");
            }
            mDevInfoService = new BluetoothGattDeviceInformationService();
            try {
                mBluetoothGattServer.addService(mDevInfoService);
            } catch (Exception e4) {
                Log.e(TAG, "Unable to add Device Information Service");
            }
        }
    }

    private void stopServer() {
        if (mBluetoothGattServer == null) {
            Log.e(TAG, "No Gatt server opened");
            return;
        }
        if (mHIDService != null) {
            mBluetoothGattServer.removeService(mHIDService);
        }
        if (mBatteryService != null) {
            mBluetoothGattServer.removeService(mBatteryService);
        }
        if (mDevInfoService != null) {
            mBluetoothGattServer.removeService(mDevInfoService);
        }
        mBluetoothGattServer.close();
        mBluetoothGattServer = null;
    }

    private void processNotification() {
        if (mBluetoothGattServer == null) {
            Log.e(TAG, "Abort Notification, no GATT Server");
        } else if (mConnectedDevice != null) {
            BluetoothGattCharacteristic c;
            if (mHIDService != null) {
                c = mHIDService.getNotification();
                if (!(c == null || c.getValue() == null)) {
                    mBluetoothGattServer.notifyCharacteristicChanged(mConnectedDevice, c, DBG);
                }
            }
            if (mBatteryService != null) {
                c = mBatteryService.getNotification();
                if (c != null && c.getValue() != null) {
                    mBluetoothGattServer.notifyCharacteristicChanged(mConnectedDevice, c, DBG);
                }
            }
        }
    }

    public void setButtonLeft(boolean down) {
        if (mHIDService == null) {
            Log.e(TAG, "no HID Service");
            return;
        }
        mHIDService.setButton(0, down);
        processNotification();
    }

    public void moveXY(float dX, float dY) {
        if (mHIDService == null) {
            Log.e(TAG, "no HID Service");
            return;
        }
        mHIDService.setXYDisplacement((int) (((double) dX) * mSensitivity), (int) (((double) dY) * mSensitivity));
        processNotification();
        mHIDService.setXYDisplacement(0, 0);
    }

    public BluetoothDevice getConnectedDevice() {
        return mConnectedDevice;
    }

    private void sendStateChanged() {
        sendBroadcast(new Intent(STATE_CHANGED));
    }
}
