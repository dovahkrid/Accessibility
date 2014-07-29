package com.litan.accessibilitytest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PerformListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final IAccessTestService service = (IAccessTestService) getApplicationContext().getSystemService(App.SERVICE_AC);
        if (service == null) {
            Log.e("litan", "can not bind service");
            finish();
            return;
        }
        setContentView(R.layout.fragment_main);
        ListView listView = (ListView) findViewById(android.R.id.list);
        try {
            List<String> pkgList = service.getRecordedPkg();
            final List<PackageInfo> pkgInfoList = new ArrayList<PackageInfo>();
            final PackageManager pm = getPackageManager();
            for (String p : pkgList) {
                PackageInfo pkgInfo = pm.getPackageInfo(p, 0);
                if (pkgInfo != null) {
                    pkgInfoList.add(pkgInfo);
                }
            }
            AppListAdapter adapter = new AppListAdapter(this, pm, pkgInfoList);
            listView.setAdapter(adapter);     
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String pkg = pkgInfoList.get(position).packageName;
                    Intent intent = pm.getLaunchIntentForPackage(pkg);
                    if (intent == null) {
                        Toast.makeText(PerformListActivity.this, "can not found intent", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        service.startPerform(pkg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
