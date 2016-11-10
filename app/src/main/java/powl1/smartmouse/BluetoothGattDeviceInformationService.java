package powl1.smartmouse;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import java.util.UUID;

class BluetoothGattDeviceInformationService extends BluetoothGattService {
    private static final UUID DeviceInformationUUID =
            UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    private static final UUID PnPIDUUID =
            UUID.fromString("00002A50-0000-1000-8000-00805f9b34fb");

    private static BluetoothGattCharacteristic mPnPID =
            new BluetoothGattCharacteristic(PnPIDUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);

    private static final byte[] PNP_ID = new byte[] {
            (byte) 0x00,              // Vendor ID Source
            (byte) 0x86, (byte) 0x80, // Vendor ID
            (byte) 0x01, (byte) 0x00, // Product ID
            (byte) 0x01, (byte) 0x00  // Product Version
    };

    BluetoothGattDeviceInformationService() {
        super(DeviceInformationUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        mPnPID.setValue(PNP_ID);
        addCharacteristic(mPnPID);
    }
}
