package com.okii.bluetoothle;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class BleService extends Service {

    public static final String TAG = "BleService";
    static final int MSG_REGISTER = 1;
    static final int MSG_UNREGISTER = 2;
    private final Messenger mMessenger;
    private final List<Messenger> mClients = new LinkedList<Messenger>();



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
                    default:
                        super.handleMessage(msg);
                }
            }

        }
    }


}
