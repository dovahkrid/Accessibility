
package com.litan.accessibilitytest;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListAdapter extends BaseAdapter implements
        android.widget.AdapterView.OnItemClickListener {
    private final List<PackageInfo> mPkgList;
    private final Context mCtx;
    private final PackageManager mPm;

    AppListAdapter(Context ctx, PackageManager pm, List<PackageInfo> pkgList) {
        if (ctx == null || pm == null || pkgList == null) {
            throw new NullPointerException();
        }
        mPm = pm;
        mCtx = ctx;
        mPkgList = pkgList;
    }

    @Override
    public int getCount() {
        return mPkgList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPkgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.list_item, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
        TextView tv = (TextView) convertView.findViewById(R.id.title);
        PackageInfo pkg = mPkgList.get(position);
        imageView.setImageDrawable(pkg.applicationInfo.loadIcon(mPm));
        tv.setText(pkg.applicationInfo.loadLabel(mPm));
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = getAppDetailsIntent(mPkgList.get(position).packageName);
        if (isActivityAvailable(mCtx, intent)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mCtx.startActivity(intent);
        }
    }

    public static boolean isActivityAvailable(Context cxt, Intent intent) {
        List<ResolveInfo> list = cxt.getPackageManager().queryIntentActivities(intent, 0);
        return list != null && list.size() > 0;
    }

    public static Intent getAppDetailsIntent(String pkgName) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= 9) {
            intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", pkgName, null));
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings",
                    "com.android.settings.InstalledAppDetails");
            if (Build.VERSION.SDK_INT == 8) {
                intent.putExtra("pkg", pkgName);
            } else { // SDK_VERSION == 7
                intent.putExtra("com.android.settings.ApplicationPkgName", pkgName);
            }
        }
        return intent;
    }
}
