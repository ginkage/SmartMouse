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

    static final UUID HIDServiceUUID =
            UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");

    private static final UUID ProtocolModeUUID =
            UUID.fromString("00002A4E-0000-1000-8000-00805f9b34fb");

    private static final UUID BootMouseInputReportUUID =
            UUID.fromString("00002A33-0000-1000-8000-00805f9b34fb");

    private static final UUID ConfigUUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final UUID HIDInfoUUID =
            UUID.fromString("00002A4a-0000-1000-8000-00805f9b34fb");

    private static final UUID ReportMapUUID =
            UUID.fromString("00002A4b-0000-1000-8000-00805f9b34fb");

    private static final UUID ReportUUID =
            UUID.fromString("00002A4d-0000-1000-8000-00805f9b34fb");

    private static final UUID ReportRefUUID =
            UUID.fromString("00002908-0000-1000-8000-00805f9b34fb");

    private static BootMouseReport mBootMouseReport = new BootMouseReport();

    private static BluetoothGattCharacteristic mProtocolMode =
            new BluetoothGattCharacteristic(ProtocolModeUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ
                            | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ
                            | BluetoothGattCharacteristic.PERMISSION_WRITE);

    private static BluetoothGattCharacteristic mBootMouseInputReport =
            new BluetoothGattCharacteristic(BootMouseInputReportUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ
                            | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);

    private static BluetoothGattDescriptor mBootMouseInputReportConfig =
            new BluetoothGattDescriptor(ConfigUUID,
                    BluetoothGattDescriptor.PERMISSION_READ
                            | BluetoothGattDescriptor.PERMISSION_WRITE);

    private static BluetoothGattCharacteristic mHIDInformation =
            new BluetoothGattCharacteristic(HIDInfoUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);

    private static BluetoothGattCharacteristic mReportMap =
            new BluetoothGattCharacteristic(ReportMapUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);

    private static BluetoothGattCharacteristic mMouseInputReport =
            new BluetoothGattCharacteristic(ReportUUID,
                    BluetoothGattCharacteristic.PROPERTY_READ
                            | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);

    private static BluetoothGattDescriptor mMouseInputReportConfig =
            new BluetoothGattDescriptor(ConfigUUID,
                    BluetoothGattDescriptor.PERMISSION_READ
                            | BluetoothGattDescriptor.PERMISSION_WRITE);

    private static BluetoothGattDescriptor mMouseInputReportRef =
            new BluetoothGattDescriptor(ReportRefUUID,
                    BluetoothGattDescriptor.PERMISSION_READ);

    private static final byte[] MOUSE_REPORT_MAP_DESCRIPTOR = {
            (byte) 0x05, (byte) 0x01, // Usage Page (Generic Desktop)
            (byte) 0x09, (byte) 0x02, // Usage (Mouse)
            (byte) 0xA1, (byte) 0x01, // Collection (Application)
//            (byte) 0x85, (byte) 0x01, //    Report ID 01
            (byte) 0x09, (byte) 0x01, //    Usage (Pointer)
            (byte) 0xA1, (byte) 0x00, //    Collection (Physical)
            (byte) 0x05, (byte) 0x09, //       Usage Page (Buttons)
            (byte) 0x19, (byte) 0x01, //       Usage minimum (1)
            (byte) 0x29, (byte) 0x03, //       Usage maximum (3)
            (byte) 0x15, (byte) 0x00, //       Logical minimum (0)
            (byte) 0x25, (byte) 0x01, //       Logical maximum (1)
            (byte) 0x75, (byte) 0x01, //       Report size (1)
            (byte) 0x95, (byte) 0x03, //       Report count (3)
            (byte) 0x81, (byte) 0x02, //       Input (Data, Variable, Absolute)
            (byte) 0x75, (byte) 0x05, //       Report size (5)
            (byte) 0x95, (byte) 0x01, //       Report count (1)
            (byte) 0x81, (byte) 0x01, //       Input (Constant)                 ; 5 bit padding
            (byte) 0x05, (byte) 0x01, //       Usage page (Generic Desktop)
            (byte) 0x09, (byte) 0x30, //       Usage (X)
            (byte) 0x09, (byte) 0x31, //       Usage (Y)
            (byte) 0x09, (byte) 0x38, //       Usage (Wheel)
            (byte) 0x15, (byte) 0x81, //       Logical minimum (-127)
            (byte) 0x25, (byte) 0x7F, //       Logical maximum (127)
            (byte) 0x75, (byte) 0x08, //       Report size (8)
            (byte) 0x95, (byte) 0x03, //       Report count (3)
            (byte) 0x81, (byte) 0x06, //       Input (Data, Variable, Relative)
            (byte) 0xC0,              //    End Collection
            (byte) 0xC0,              // End Collection
/*
            (byte) 0x05, (byte) 0x01, // Usage page (Generic Desktop)
            (byte) 0x09, (byte) 0x06, // Usage (Keyboard)
            (byte) 0xA1, (byte) 0x01, // Collection (Application)
            (byte) 0x85, (byte) 0x02, //    Report ID 02
            (byte) 0x05, (byte) 0x07, //       Usage page (Key Codes)
            (byte) 0x19, (byte) 0xE0, //       Usage minimum (224)
            (byte) 0x29, (byte) 0xE7, //       Usage maximum (231)
            (byte) 0x15, (byte) 0x00, //       Logical minimum (0)
            (byte) 0x25, (byte) 0x01, //       Logical maximum (1)
            (byte) 0x75, (byte) 0x01, //       Report size (1)
            (byte) 0x95, (byte) 0x08, //       Report count (8)
            (byte) 0x81, (byte) 0x02, //       Input (Data, Variable, Absolute) ; Modifier byte
            (byte) 0x75, (byte) 0x08, //       Report size (8)
            (byte) 0x95, (byte) 0x01, //       Report count (1)
            (byte) 0x81, (byte) 0x01, //       Input (Constant)                 ; Reserved byte
            (byte) 0x75, (byte) 0x08, //       Report size (8)
            (byte) 0x95, (byte) 0x06, //       Report count (6)
            (byte) 0x15, (byte) 0x00, //       Logical Minimum (0)
            (byte) 0x25, (byte) 0x65, //       Logical Maximum (101)
            (byte) 0x05, (byte) 0x07, //       Usage page (Key Codes)
            (byte) 0x19, (byte) 0x00, //       Usage Minimum (0)
            (byte) 0x29, (byte) 0x65, //       Usage Maximum (101)
            (byte) 0x81, (byte) 0x00, //       Input (Data, Array)              ; Key array (6 keys)
            (byte) 0xC0               // End Collection
*/
    };

    private static final byte[] REPORT_REF_MOUSE = { (byte) 0, (byte) 1 };

    private static final byte[] HID_INFO = {
            0x13, 0x02, // Version number of base USB HID Specification.
            0x00,       // Country code
            0x02        // Flags (Normally connectable)
    };

    private static final byte[] DESCRIPTOR_CONFIG_NONE = { (byte) 0, (byte) 0 };
    private static final byte[] DESCRIPTOR_CONFIG_NOTIFY = { MASK_NOTIFICATION, (byte) 0 };

    BluetoothGattHIDService() {
        super(HIDServiceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

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

        mMouseInputReportConfig.setValue(DESCRIPTOR_CONFIG_NOTIFY);
        mMouseInputReport.addDescriptor(mMouseInputReportConfig);
        mMouseInputReportRef.setValue(REPORT_REF_MOUSE);
        mMouseInputReport.addDescriptor(mMouseInputReportRef);
        mMouseInputReport.setValue(mBootMouseReport.getRawValue());
        addCharacteristic(mMouseInputReport);
    }

    private void setProtocolMode(int protocolMode) {
        mProtocolMode.setValue(new byte[] { (byte) protocolMode });
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
                break;
            case 1 /*1*/:
                mBootMouseReport.setButton2(value);
                break;
            case 2:
                mBootMouseReport.setButton3(value);
                break;
            default:
                break;
        }
    }

    private boolean isNotifEnabled(BluetoothGattDescriptor d) {
        return d.getValue() != null && (d.getValue()[0] & MASK_NOTIFICATION) == MASK_NOTIFICATION;
    }

    BluetoothGattCharacteristic getNotification() {
        int mode = mProtocolMode.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
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
