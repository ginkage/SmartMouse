package powl1.smartmouse;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import java.util.UUID;

class BluetoothGattDeviceInformationService extends BluetoothGattService {
    private static final UUID DeviceInformationUUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    private static final UUID PnPIDUUID = UUID.fromString("00002A50-0000-1000-8000-00805f9b34fb");
    private static BluetoothGattCharacteristic mPnPID = new BluetoothGattCharacteristic(PnPIDUUID, 2, 1);
    private static final byte[] PNP_ID = new byte[]{(byte) 0, (byte) -122, Byte.MIN_VALUE, (byte) 1, (byte) 0, (byte) 1, (byte) 0};

    BluetoothGattDeviceInformationService() {
        super(DeviceInformationUUID, 0);
        mPnPID.setValue(PNP_ID);
        addCharacteristic(mPnPID);
    }
}
