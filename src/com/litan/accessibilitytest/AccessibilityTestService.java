
package com.litan.accessibilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class AccessibilityTestService extends AccessibilityService {
    private static final String TAG = "litan";
    private boolean mIsOkClicked;
    private boolean mIsStopClicked;
    private Toast mToast;

    private void log(AccessibilityNodeInfo node, int level) {
        logd(level + " node:" + node.getChildCount() + node.getViewIdResourceName());
        for (int i = 0;i<node.getChildCount();i++) {
            AccessibilityNodeInfo n = node.getChild(i);
            log(n, level + 1);
        }
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        logi("onAccessibilityEvent:" + event);
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        } else {
            log(source, 0);
        }
//        if (true)
//        return;
        // just test code
//        if ("com.example.androidtest".equals(event.getPackageName())) {
//            log(source, 0);
//        } else {
//            return;
//        }
        // read chrome browser addr
        if ("com.android.chrome".equals(event.getPackageName())) {
            log(source, 0);
//            String str = "";
//            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
//                str = "TEXT_SELECTION_CHANGED:" + event.getText().toString();
//            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
//                str = "TEXT_CHANGED:" + event.getText().toString();
//            }
//            if (!TextUtils.isEmpty(str)) {
//                if (mToast == null) {
//                    mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
//                }
//                mToast.setText( str);
//                logd(str);
//                mToast.show();
//            }
            return;
        }
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        logd("AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED");
        if (!"com.android.settings".equals(event.getPackageName())) {
            logd("Non settings");
            return;
        }
        if ("com.android.settings.applications.InstalledAppDetailsTop".equals(event.getClassName())) {
            logd("enter InstalledAppDetailsTop");
//            AccessibilityNodeInfo node = findNode(source, new String[] {
//                    "com.android.settings:id/left_button",
//                    "com.android.settings:id/force_stop_button"
                    AccessibilityNodeInfo node = findNode(source, new String[] {
                            "com.android.settings:id/notification_switch"
            }, "显示通知");
            if (node != null) {
                if (mIsOkClicked) {
                    mIsOkClicked = false;
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    return;
                }
                if (node.isEnabled() && node.isChecked()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    logi("perform 强行停止");
                    mIsStopClicked = true;
                }
                return;
            }
        } else if ("android.app.AlertDialog".equals(event.getClassName())) {
            logd("enter AlertDialog");
            AccessibilityNodeInfo node = findNode(source, new String[] {
                    "android:id/button1"
            }, "确定");
            if (node != null) {
                if (mIsStopClicked) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    logi("perform 确定");
                    mIsOkClicked = true;
                    mIsStopClicked = false;
                }
            }
            return;
        }
    }

    private AccessibilityNodeInfo findNode(AccessibilityNodeInfo source, String[] ids, String text) {
        if (source == null || ids == null || text == null) {
            throw new NullPointerException();
        }
        List<AccessibilityNodeInfo> nodeList = null;
        if (Build.VERSION.SDK_INT >= 18) {
            for (String id : ids) {
                nodeList = source
                        .findAccessibilityNodeInfosByViewId(id);
                if (nodeList != null && !nodeList.isEmpty()) {
                    return nodeList.get(0);
                }
            }
        } else if (Build.VERSION.SDK_INT >= 14) {
            nodeList = source.findAccessibilityNodeInfosByText(text);
            if (nodeList != null && !nodeList.isEmpty()) {
                return nodeList.get(0);
            }
        }
        logw("Can not found node:" + text);
        return null;
    }

    @Override
    public void onInterrupt() {
        logi("onInterrupt");

    }

    @Override
    public void onServiceConnected() {
        logi("onServiceConnected");
        // AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        // | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        // info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // info.notificationTimeout = 80;
        // setServiceInfo(info);
    }

    private static void logd(String msg) {
        Log.d(TAG, msg);
    }

    private static void logw(String msg) {
        Log.w(TAG, msg);
    }

    private static void logi(String msg) {
        Log.i(TAG, msg);
    }
}
