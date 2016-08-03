package com.okii.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;

public class BleActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothLE";
    private final Messenger mMessenger;
    private Intent mServiceIntent;
    private Messenger mService = null;
    private final int ENABLE_BT = 1;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null,BleService.MSG_REGISTER);
                if (msg != null){
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }else {
                    mService = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.w(TAG,"Error connecting to BleService",e);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    public BleActivity(){
        super();
        mMessenger = new Messenger(new IncomingHandler(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        mServiceIntent = new Intent(this,BleService.class);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(mServiceIntent,mConnection,BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mService != null){
            try {
                Message msg = Message.obtain(null,BleService.MSG_UNREGISTER);
                if (msg != null){
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
            } catch (RemoteException e) {
                Log.w(TAG,"Error unregistering with BleService",e);
                mService = null;
                e.printStackTrace();
            } finally {
                unbindService(mConnection);
            }
        }
    }

    private static class IncomingHandler extends Handler{
        private final WeakReference<BleActivity> mActivity;
        public IncomingHandler(BleActivity activity){
            mActivity = new WeakReference<BleActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BleActivity activity = mActivity.get();
            if (activity != null){
                //TODO: Do something
            }
            super.handleMessage(msg);
        }
    }

    private void enableBluetooth(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent,ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ENABLE_BT){
            if (resultCode == RESULT_OK){
                //蓝牙连接，继续
                startScan();
            }else {
                finish();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void startScan(){
        //TODO: Do something

        Message msg = Message.obtain(null,BleService.MSG_START_SCAN);
        if (msg != null){
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG,"Lost connection to service",e);
                unbindService(mConnection);
            }
        }
    }
}
