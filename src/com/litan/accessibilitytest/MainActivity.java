
package com.litan.accessibilitytest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
//    private IAccessTestService mService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
//        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
//        List<AccessibilityServiceInfo> installedServices = accessibilityManager.getInstalledAccessibilityServiceList();
//        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(this);
//        final boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(),
//                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
//        for (int i = 0, count = installedServices.size(); i < count; ++i) {
//            AccessibilityServiceInfo info = installedServices.get(i);
//
//            String title = info.getResolveInfo().loadLabel(getPackageManager()).toString();
//
//            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
//            ComponentName componentName = new ComponentName(serviceInfo.packageName,
//                    serviceInfo.name);
//
//            String key = componentName.flattenToString();
//            //ComponentName toggledService = ComponentName.unflattenFromString(key);
//
//            final boolean serviceEnabled = accessibilityEnabled
//                    && enabledServices.contains(componentName);
//
//           // String description = info.loadDescription(getPackageManager());
//            //String settingsClassName = info.getSettingsActivityName();
//            Log.d("litan", "title:" + title + " key:" + key + " servieEnabled:" + serviceEnabled);
//        }
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        private ListView mListView;
        private boolean mStartRecord;
        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_start_record) {
                mStartRecord = true;
                mListView.setBackgroundColor(Color.GREEN);
                Toast.makeText(getActivity(), "start record", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_start_perform) {
                mListView.setBackgroundColor(Color.WHITE);
                Toast.makeText(getActivity(), "start perform", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), PerformListActivity.class);
                startActivity(intent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView listView = (ListView) rootView.findViewById(android.R.id.list);
            mListView = listView;
            PackageManager pm = getActivity().getPackageManager();
            List<PackageInfo> pkgList = pm.getInstalledPackages(0);
            final List<PackageInfo> newPkgList = new ArrayList<PackageInfo>();
            final List<Intent> launchIntents = new ArrayList<Intent>();
            for (PackageInfo pkgInfo : pkgList) {
                Intent intent = pm.getLaunchIntentForPackage(pkgInfo.packageName);
                if (intent == null) {
                    //Log.e("litan", "can not find lauch intent for " + pkgInfo.packageName);
                } else {
                    launchIntents.add(intent);
                    newPkgList.add(pkgInfo);
                }
            }
            final CopyAdapter adapter = new CopyAdapter(getActivity(), launchIntents, newPkgList, pm);
           // final LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = launchIntents.get(position);
                    //Intent intent = (Intent) adapter.getItem(position);
                    if (mStartRecord) {
                        IAccessTestService service = (IAccessTestService) getApplicationContext().getSystemService(App.SERVICE_AC);
                        if (service != null) {
                            try {
                                service.startRecord(newPkgList.get(position).packageName);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            mStartRecord = false;
                            mListView.setBackgroundColor(Color.WHITE);
                        } else {
                            Log.e("litan", "service not bound");
                            mStartRecord = false;
                            mListView.setBackgroundColor(Color.WHITE);
                            return;
                        } 
//                        Intent i = new Intent(AccessibilityTestService.ACTION_START_RECORD);
//                        i.putExtra("pkg", newPkgList.get(position).packageName);
//                        mgr.sendBroadcast(i);
//                        mStartRecord = false;
//                        mListView.setBackgroundColor(Color.WHITE);
                    }
                    getActivity().startActivity(intent);
                }
            });
//            Button bt = new Button(getActivity());
//            bt.setText("show view");
//            bt.setOnClickListener(new OnClickListener(){
//
//                @Override
//                public void onClick(View v) {
//                    LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(getActivity());
//                    mgr.sendBroadcast(new Intent("show.window"));
//                }
//                
//            });
            return rootView;
        }
    }

}
