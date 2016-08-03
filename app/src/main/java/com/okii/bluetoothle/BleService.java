package com.okii.bluetoothle;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.BundleCompat;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BleService extends Service implements BluetoothAdapter.LeScanCallback{

    public static final String TAG = "BleService";
    static final int MSG_REGISTER = 1;
    static final int MSG_UNREGISTER = 2;
    static final int MSG_START_SCAN = 3;
    static final int MSG_DEVICE_FOND = 4;
    static final String KEY_MAC_ADDRESS = "KEY_MAC_ADDRESS";
    private final Messenger mMessenger;
    private final List<Messenger> mClients = new LinkedList<Messenger>();

    private final Map<String,BluetoothDevice> mDevices = new HashMap<String, BluetoothDevice>();

    public enum State{
        UNKNOWN,
        IDLE,
        SCANNING,
        BLUETOOTH_OFF,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    private BluetoothAdapter mBluetooth = null;
    private  State mState = State.UNKNOWN;
    private Handler mHandler = new Handler();
    private static final int SCAN_PERIOD = 10000;
    private static final String DEVICE_NAME  = "SensorTag";


    public BleService() {
        mMessenger = new Messenger(new IncomingHandler(this));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mMessenger.getBinder();
    }

    private static class IncomingHandler extends Handler{

        private final WeakReference<BleService> mService;

        public IncomingHandler(BleService service){
            mService = new WeakReference<BleService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            //拿到父类BleService的引用，以便访问父类的成员变量
            BleService service = mService.get();
            if (service != null){
                switch (msg.what){
                    case MSG_REGISTER:
                        service.mClients.add(msg.replyTo);
                        Log.d(TAG,"已注册");
                        break;
                    case MSG_UNREGISTER:
                        service.mClients.remove(msg.replyTo);
                        Log.d(TAG,"已取消注册");
                        break;
                    case MSG_START_SCAN:
                        service.startScan();
                        Log.d(TAG,"Start Scan");
                    default:
                        super.handleMessage(msg);
                }
            }

        }
    }

    public void startScan(){
        mDevices.clear();
        setState(State.SCANNING);
        if (mBluetooth == null){
            BluetoothManager bluetoothMgr = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
            mBluetooth = bluetoothMgr.getAdapter();
        }
        if (mBluetooth == null || !mBluetooth.isEnabled()){
            setState(State.BLUETOOTH_OFF);
        }else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mState == State.SCANNING){
                        mBluetooth.stopLeScan(BleService.this);
                        setState(State.IDLE);
                    }
                }
            },SCAN_PERIOD);
            mBluetooth.startLeScan(this);
        }

    }

    public void setState(State state){
        if (mState != state){
            mState = state;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device != null && !mDevices.containsValue(device) && device.getName() != null && device.getName().equals(DEVICE_NAME)){
            mDevices.put(device.getAddress(),device);
            Message msg = Message.obtain(null,MSG_DEVICE_FOND);
            if (msg != null){
                Bundle bundle = new Bundle();
                String[] addresses = mDevices.keySet().toArray(new String[mDevices.size()]);
                bundle.putStringArray(KEY_MAC_ADDRESS,addresses);
                msg.setData(bundle);
               sendMessage(msg);
            }
        }
    }

    private void sendMessage(Message msg){
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
