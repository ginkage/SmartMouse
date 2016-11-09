package powl1.smartmouse;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import java.util.UUID;

class BluetoothGattBatteryService extends BluetoothGattService {
    private static final byte MASK_NOTIFICATION = (byte) 1;
    private static final UUID BatteryServiceUUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID BatteryLevelUUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private static final UUID BatteryLevelConfigUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static BluetoothGattCharacteristic mBatteryLevel = new BluetoothGattCharacteristic(BatteryLevelUUID, 18, 1);
    private static BluetoothGattDescriptor mBatteryLevelConfig = new BluetoothGattDescriptor(BatteryLevelConfigUUID, 17);

    BluetoothGattBatteryService() {
        super(BatteryServiceUUID, 0);
        mBatteryLevel.setValue(new byte[]{(byte) 0});
        enableBatteryLevelNotification(false);
        mBatteryLevel.addDescriptor(mBatteryLevelConfig);
        addCharacteristic(mBatteryLevel);
    }

    private void enableBatteryLevelNotification(boolean enabled) {
        byte[] config = mBatteryLevelConfig.getValue();
        if (config == null) {
            config = new byte[]{(byte) 0, (byte) 0};
        }
        if (enabled) {
            config[0] = (byte) (config[0] | MASK_NOTIFICATION);
        }
        mBatteryLevelConfig.setValue(config);
    }

    private boolean isNotifEnabled(BluetoothGattDescriptor d) {
        return (d.getValue()[0] & MASK_NOTIFICATION) == MASK_NOTIFICATION;
    }

    public BluetoothGattCharacteristic getNotification() {
        if (isNotifEnabled(mBatteryLevelConfig)) {
            return mBatteryLevel;
        }
        return null;
    }
}
