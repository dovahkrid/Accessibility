package com.litan.accessibilitytest;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CopyAdapter extends BaseAdapter {
    private final Context mCtx;
    private final List<Intent> mList;
    private final List<PackageInfo> mPkgList;
    private final PackageManager mPm;
    public CopyAdapter(Context ctx, List<Intent> intentList, List<PackageInfo> pkgList, PackageManager pm) {
        mCtx = ctx;
        mList = intentList;
        mPkgList = pkgList;
        mPm = pm;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
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

}
