package powl1.smartmouse;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import java.util.UUID;

class BluetoothGattHIDService extends BluetoothGattService {
    private static final byte MASK_NOTIFICATION = (byte) 1;
    private static final int PROTOCOL_MODE_BOOT = 0;
    private static final int PROTOCOL_MODE_DEFAULT = 1;
    private static final int PROTOCOL_MODE_REPORT = 1;
    private static final UUID HIDServiceUUID = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");
    private static final UUID ProtocolModeUUID = UUID.fromString("00002A4E-0000-1000-8000-00805f9b34fb");
    private static final UUID BootMouseInputReportUUID = UUID.fromString("00002A33-0000-1000-8000-00805f9b34fb");
    private static final UUID ConfigUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID HIDInfoUUID = UUID.fromString("00002A4a-0000-1000-8000-00805f9b34fb");
    private static final UUID ReportMapUUID = UUID.fromString("00002A4b-0000-1000-8000-00805f9b34fb");
    private static final UUID ReportUUID = UUID.fromString("00002A4d-0000-1000-8000-00805f9b34fb");
    private static final UUID ReportRefUUID = UUID.fromString("00002908-0000-1000-8000-00805f9b34fb");
    private static BootMouseReport mBootMouseReport = new BootMouseReport();
    private static BluetoothGattCharacteristic mProtocolMode = new BluetoothGattCharacteristic(ProtocolModeUUID, 6, 17);
    private static BluetoothGattCharacteristic mBootMouseInputReport = new BluetoothGattCharacteristic(BootMouseInputReportUUID, 18, 1);
    private static BluetoothGattDescriptor mBootMouseInputReportConfig = new BluetoothGattDescriptor(ConfigUUID, 17);
    private static BluetoothGattCharacteristic mHIDInformation = new BluetoothGattCharacteristic(HIDInfoUUID, 2, 1);
    private static BluetoothGattCharacteristic mReportMap = new BluetoothGattCharacteristic(ReportMapUUID, 2, 1);
    private static BluetoothGattCharacteristic mMouseInputReport = new BluetoothGattCharacteristic(ReportUUID, 18, 1);
    private static BluetoothGattDescriptor mMouseInputReportConfig = new BluetoothGattDescriptor(ConfigUUID, 17);
    private static BluetoothGattDescriptor mMouseInputReportRef = new BluetoothGattDescriptor(ReportRefUUID, 1);
    private static final byte[] MOUSE_REPORT_MAP_DESCRIPTOR = new byte[]{(byte) 5, (byte) 1, (byte) 9, (byte) 2, (byte) -95, (byte) 1, (byte) 9, (byte) 1, (byte) -95, (byte) 0, (byte) 5, (byte) 9, (byte) 25, (byte) 1, (byte) 41, (byte) 3, (byte) 21, (byte) 0, (byte) 37, (byte) 1, (byte) -107, (byte) 3, (byte) 117, (byte) 1, (byte) -127, (byte) 2, (byte) -107, (byte) 1, (byte) 117, (byte) 5, (byte) -127, (byte) 3, (byte) 5, (byte) 1, (byte) 9, (byte) 48, (byte) 9, (byte) 49, (byte) 21, (byte) -127, (byte) 37, Byte.MAX_VALUE, (byte) 117, (byte) 8, (byte) -107, (byte) 2, (byte) -127, (byte) 6, (byte) -64, (byte) -64};
    private static final byte[] REPORT_REF_MOUSE = new byte[]{(byte) 0, (byte) 1};
    private static final byte[] HID_INFO = new byte[]{(byte) 19, (byte) 2, (byte) 0, (byte) 2};
    private static final byte[] DESCRIPTOR_CONFIG_NONE = new byte[]{(byte) 0, (byte) 0};
    private static final byte[] DESCRIPTOR_CONFIG_NOTIFY = new byte[]{MASK_NOTIFICATION, (byte) 0};

    BluetoothGattHIDService() {
        super(HIDServiceUUID, 0);
        provision();
    }

    private void provision() {
        setProtocolMode(PROTOCOL_MODE_DEFAULT);
        addCharacteristic(mProtocolMode);
        mBootMouseInputReportConfig.setValue(DESCRIPTOR_CONFIG_NONE);
        mBootMouseInputReport.addDescriptor(mBootMouseInputReportConfig);
        mBootMouseInputReport.setValue(mBootMouseReport.getRawValue());
        addCharacteristic(mBootMouseInputReport);
        mHIDInformation.setValue(HID_INFO);
        addCharacteristic(mHIDInformation);
        mReportMap.setValue(MOUSE_REPORT_MAP_DESCRIPTOR);
        addCharacteristic(mReportMap);
        mBootMouseInputReportConfig.setValue(DESCRIPTOR_CONFIG_NOTIFY);
        mMouseInputReport.addDescriptor(mMouseInputReportConfig);
        mMouseInputReportRef.setValue(REPORT_REF_MOUSE);
        mMouseInputReport.addDescriptor(mMouseInputReportRef);
        mMouseInputReport.setValue(mBootMouseReport.getRawValue());
        addCharacteristic(mMouseInputReport);
    }

    private void setProtocolMode(int protocolMode) {
        byte[] value = new byte[1];
        value[0] = (byte) protocolMode;
        mProtocolMode.setValue(value);
    }

    void setXYDisplacement(int x, int y) {
        mBootMouseReport.setXDisplacement(x);
        mBootMouseReport.setYDisplacement(y);
        mBootMouseInputReport.setValue(mBootMouseReport.getRawValue());
        mMouseInputReport.setValue(mBootMouseReport.getRawValue());
    }

    void setButton(int id, boolean value) {
        switch (id) {
            case 0 /*0*/:
                mBootMouseReport.setButton1(value);
            case 1 /*1*/:
                mBootMouseReport.setButton2(value);
            case 2:
                mBootMouseReport.setButton3(value);
            default:
        }
    }

    private boolean isNotifEnabled(BluetoothGattDescriptor d) {
        return d.getValue() != null && (d.getValue()[0] & MASK_NOTIFICATION) == MASK_NOTIFICATION;
    }

    BluetoothGattCharacteristic getNotification() {
        int mode = mProtocolMode.getIntValue(17, 0);
        mMouseInputReport.setValue(mBootMouseReport.getRawValue());
        if (mode == PROTOCOL_MODE_BOOT && isNotifEnabled(mBootMouseInputReportConfig)) {
            return mBootMouseInputReport;
        }
        if (mode == PROTOCOL_MODE_REPORT && isNotifEnabled(mMouseInputReportConfig)) {
            return mMouseInputReport;
        }
        return null;
    }
}
