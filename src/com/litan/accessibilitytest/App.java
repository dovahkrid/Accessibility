package com.litan.accessibilitytest;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class App extends Application {
    public static final String SERVICE_AC = "ac";
private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("litan", "APP:onReceive");
            Bundle bundle = intent.getBundleExtra("bundle");
            IBinder binder = (Binder) bundle.getBinder("binder");
            mService = IAccessTestService.Stub.asInterface(binder);
        }
    };
    private IAccessTestService mService;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("litan", "APP:oncreate");
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.binder");
        mgr.registerReceiver(mReceiver, filter);
    }

    @Override
    public Object getSystemService(String name) {
        if (SERVICE_AC.equals(name)) {
            return mService;
        }
        return super.getSystemService(name);
    }

}
