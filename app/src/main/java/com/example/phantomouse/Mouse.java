//package com.example.phantomouse;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothHidDevice;
//import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
//import android.bluetooth.BluetoothProfile;
//import android.content.Intent;
//import android.os.Build;
//import android.widget.ArrayAdapter;
//
//import androidx.annotation.RequiresApi;
//
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.Executor;
//
//public class Mouse {
//    private static final byte[] descriptor = new byte[]{
//            0x05, 0x01,                    // USAGE_PAGE (Generic Desktop)
//            0x09, 0x02,                    // USAGE (Mouse)
//            (byte) 0xa1, 0x01,                    // COLLECTION (Application)
//            0x09, 0x01,                    //   USAGE (Pointer)
//            (byte) 0xa1, 0x00,                    //   COLLECTION (Physical)
//            0x05, 0x09,                    //     USAGE_PAGE (Button)
//            0x19, 0x01,                    //     USAGE_MINIMUM (Button 1)
//            0x29, 0x03,                    //     USAGE_MAXIMUM (Button 3)
//            0x15, 0x00,                    //     LOGICAL_MINIMUM (0)
//            0x25, 0x01,                    //     LOGICAL_MAXIMUM (1)
//            (byte) 0x95, 0x03,                    //     REPORT_COUNT (3)
//            0x75, 0x01,                    //     REPORT_SIZE (1)
//            (byte) 0x81, 0x02,                    //     INPUT (Data,Var,Abs)
//            (byte) 0x95, 0x01,                    //     REPORT_COUNT (1)
//            0x75, 0x05,                    //     REPORT_SIZE (5)
//            (byte) 0x81, 0x03,                    //     INPUT (Cnst,Var,Abs)
//            0x05, 0x01,                    //     USAGE_PAGE (Generic Desktop)
//            0x09, 0x30,                    //     USAGE (X)
//            0x09, 0x31,                    //     USAGE (Y)
//            0x15, (byte) 0x81,                    //     LOGICAL_MINIMUM (-127)
//            0x25, 0x7f,                    //     LOGICAL_MAXIMUM (127)
//            0x75, 0x08,                    //     REPORT_SIZE (8)
//            (byte) 0x95, 0x02,                    //     REPORT_COUNT (2)
//            (byte) 0x81, 0x06,                    //     INPUT (Data,Var,Rel)
//            (byte) 0xc0,                          //   END_COLLECTION
//            (byte) 0xc0                           // END_COLLECTION
//
//    };
//
//    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    private BluetoothHidDevice mBlHidDevice;
//    private BluetoothDevice mBtDevice;
//
//    private void getProxy(List<String> ListElementsArrayList, ArrayAdapter<String> adapter) {
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        }
//        // If the adapter is null, then Bluetooth is not supported
//        if (mBluetoothAdapter == null) {
//            System.out.println( "Bluetooth is not available");
//        }
//
//        mBluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
//            @RequiresApi(api = Build.VERSION_CODES.P)
//            @Override
//            public void onServiceConnected(int profile, BluetoothProfile proxy) {
//                if (profile == BluetoothProfile.HID_DEVICE) {
//                    System.out.println("Got HID device");
//                    mBlHidDevice = (BluetoothHidDevice) proxy;
//                    // see next section
//                    registerApp(ListElementsArrayList , adapter, mBlHidDevice);
//                }
//            }
//            @Override
//            public void onServiceDisconnected(int profile) {
//                if (profile == BluetoothProfile.HID_DEVICE) {
//                    System.out.println("Lost HID device");
//                }
//            }
//        }, BluetoothProfile.HID_DEVICE);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.P)
//    public synchronized void registerApp(List<String> ListElementsArrayList, ArrayAdapter<String> adapter, final BluetoothHidDevice bleHidD){
//
//        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
//                "BlueExp",
//                "Android HID Foray",
//                "Android",
//                BluetoothHidDevice.SUBCLASS1_MOUSE,
//                descriptor
//        );
////        Executor executor = Executors.newSingleThreadExecutor();
//        ///TODO: Replace below executor with above in-line
//        Executor executor = new Executor() {
//            @Override
//            public void execute(Runnable runnable) {
//                runnable.run();
//            }
//        };
//
//        ///TODO: Just use Executors.newsimgletrheadexectur() here
//        Boolean registered = bleHidD.registerApp(sdp, null, null, executor, new BluetoothHidDevice.Callback() {
//            @Override
//            public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
//
//                android.util.Log.v(TAG, "onGetReport: device=" + device + " type=" + type
//                        + " id=" + id + " bufferSize=" + bufferSize);
//            }
//
//            @Override
//            public void onSetReport (BluetoothDevice device,
//                                     byte type,
//                                     byte id,
//                                     byte[] data){
//                onScreenLog(ListElementsArrayList, adapter, "asked for set Report");
//            }
//
//            @Override
//            public void onConnectionStateChanged(BluetoothDevice device, final int state) {
//                onScreenLog(ListElementsArrayList, adapter, "onConnectionStateChanged: device= " + device + " state= " + state);
////                android.util.Log.v(TAG, "onConnectionStateChanged: device=" + device + " state=" + state);
//            }
//        });
//
//        onScreenLog(ListElementsArrayList, adapter, registered?"app registered" : "registration failed :(");
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.P)
//    private void btConnect(List<String> ListElementsArrayList, ArrayAdapter<String> adapter) {
//        BluetoothDevice device;
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        for(BluetoothDevice device1 : pairedDevices){
//            if(device1.getName().equals("MANRHIOWO")){
//                onScreenLog(ListElementsArrayList,  adapter, "got it");
//                device = device1;
//
//                android.util.Log.i(TAG, "btConnect: device=" + device);
//
//                // disconnect from everything else
//                for (BluetoothDevice btDev : mBlHidDevice.getDevicesMatchingConnectionStates(new int[]{
//                        BluetoothProfile.STATE_CONNECTING,
//                        BluetoothProfile.STATE_CONNECTED
//                })) {
//                    mBlHidDevice.disconnect(btDev);
//                }
//                if (device != null) {
//                    mBtDevice = device;
//                    Boolean connected = mBlHidDevice.connect(device);
//                    onScreenLog(ListElementsArrayList, adapter, connected?"connected!":"failed to connect :(");
//                }
//            }
//        }
//
//
//    }
//
//    private void moveCommand(List<String> ListElementsArrayList, ArrayAdapter<String> adapter){
//        byte[] data = new byte[]{
//                (byte)0, (byte)0x14, (byte)0x14,
//        };
//
//        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
//        onScreenLog(ListElementsArrayList, adapter, sent?"command sent":"command failed to send");
//
//    }
//
//    private void clickCommand(List<String> ListElementsArrayList, ArrayAdapter<String> adapter){
//        byte[] data = new byte[]{
//                (byte)0b1, (byte)0, (byte)0,
//        };
//
//        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
//        onScreenLog(ListElementsArrayList, adapter, sent?"command sent":"command failed to send");
//    }
//
//}
