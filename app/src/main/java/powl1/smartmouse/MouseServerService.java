package powl1.smartmouse;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

public class MouseServerService extends Service {
    private static final boolean DBG = false;
    private static final String TAG = MouseServerService.class.getSimpleName();

    public static final String STATE_CHANGED = "com.powl1.smartmouse.STATE_CHANGED";

    private BluetoothGattBatteryService mBatteryService;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothDevice mConnectedDevice;
    private BluetoothGattDeviceInformationService mDevInfoService;
    private BluetoothGattHIDService mHIDService;
    private double mSensitivity;

    public class MouseServerBinder extends Binder {
        MouseServerService getService() {
            return MouseServerService.this;
        }
    }

    private final IBinder mBinder = new MouseServerBinder();

    private final BluetoothGattServerCallback mGattServerCallback =
            new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectedDevice = device;
            } else if (device == mConnectedDevice
                    || newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectedDevice = null;
            }
            sendStateChanged();
            super.onConnectionStateChange(device, status, newState);

            Log.d(TAG, "onConnectionStateChange() called with: device = [" + device
                    + "], status = [" + status + "], newState = [" + newState + "]");
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);

            Log.d(TAG, "onServiceAdded() called with: status = [" + status
                    + "], service = [" + service + "]");
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            mBluetoothGattServer.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    offset != 0 ? Arrays.copyOfRange(value, offset, value.length) : value);
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            Log.d(TAG, "onCharacteristicReadRequest() called with: device = [" + device
                    + "], requestId = [" + requestId + "], offset = [" + offset
                    + "], characteristic = [" + characteristic.getUuid() + "]");
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            characteristic.setValue(value);
            mBluetoothGattServer.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);

            Log.d(TAG, "onCharacteristicWriteRequest() called with: device = [" + device
                    + "], requestId = [" + requestId + "], characteristic = ["
                    + characteristic.getUuid() + "], preparedWrite = [" + preparedWrite
                    + "], responseNeeded = [" + responseNeeded + "], offset = [" + offset
                    + "], value = [" + value[0] + "]");
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            mBluetoothGattServer.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

            Log.d(TAG, "onDescriptorReadRequest() called with: device = [" + device
                    + "], requestId = [" + requestId + "], offset = [" + offset
                    + "], descriptor = [" + descriptor.getUuid() + "] (characteristic = ["
                    + descriptor.getCharacteristic().getUuid() + "])");
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
            descriptor.setValue(value);
            mBluetoothGattServer.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite,
                    responseNeeded, offset, value);

            Log.d(TAG, "onDescriptorWriteRequest() called with: device = [" + device
                    + "], requestId = [" + requestId + "], descriptor = [" + descriptor.getUuid()
                    + "], preparedWrite = [" + preparedWrite + "], responseNeeded = ["
                    + responseNeeded + "], offset = [" + offset + "], value = [" + value[0]
                    + "] (characteristic = [" + descriptor.getCharacteristic().getUuid() + "])");
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            mBluetoothGattServer.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            super.onExecuteWrite(device, requestId, execute);

            Log.d(TAG, "onExecuteWrite() called with: device = [" + device + "], requestId = [" + requestId + "], execute = [" + execute + "]");
        }
    };

    public MouseServerService() {
        mSensitivity = 1.5d;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
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
        mHIDService.setXYDisplacement(
                (int) (((double) dX) * mSensitivity),
                (int) (((double) dY) * mSensitivity));
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
