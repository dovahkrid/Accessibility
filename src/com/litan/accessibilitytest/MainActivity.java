
package com.litan.accessibilitytest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> installedServices = accessibilityManager.getInstalledAccessibilityServiceList();
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(this);
        final boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
        for (int i = 0, count = installedServices.size(); i < count; ++i) {
            AccessibilityServiceInfo info = installedServices.get(i);

            String title = info.getResolveInfo().loadLabel(getPackageManager()).toString();

            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            ComponentName componentName = new ComponentName(serviceInfo.packageName,
                    serviceInfo.name);

            String key = componentName.flattenToString();
            //ComponentName toggledService = ComponentName.unflattenFromString(key);

            final boolean serviceEnabled = accessibilityEnabled
                    && enabledServices.contains(componentName);

           // String description = info.loadDescription(getPackageManager());
            //String settingsClassName = info.getSettingsActivityName();
            Log.d("litan", "title:" + title + " key:" + key + " servieEnabled:" + serviceEnabled);
        }
//        StringBuilder enabledServicesBuilder = new StringBuilder();
//        enabledServices.clear();
//        for (ComponentName enabledService : enabledServices) {
//            enabledServicesBuilder.append(enabledService.flattenToString());
//            enabledServicesBuilder.append(':');
//        }
//        final int enabledServicesBuilderLength = enabledServicesBuilder.length();
//        if (enabledServicesBuilderLength > 0) {
//            enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
//        }
//        Settings.Secure.putString(getContentResolver(),
//                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
//                enabledServicesBuilder.toString());
//
//        // Update accessibility enabled.
//        Settings.Secure.putInt(getContentResolver(),
//                Settings.Secure.ACCESSIBILITY_ENABLED, false ? 1 : 0);
    }
    static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        final String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }

        final Set<ComponentName> enabledServices = new HashSet<ComponentName>();
        final SimpleStringSplitter colonSplitter =  new SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            final String componentNameString = colonSplitter.next();
            final ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }

        return enabledServices;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            Button bt = new Button(getActivity());
            bt.setText("show view");
            bt.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(getActivity());
                    mgr.sendBroadcast(new Intent("show.window"));
                }
                
            });
            return bt;
        }
    }

}
