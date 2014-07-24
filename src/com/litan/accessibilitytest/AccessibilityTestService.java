
package com.litan.accessibilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityTestService extends AccessibilityService {
    private static final String TAG = "litan";
    private boolean m163Clicked;
    private boolean mPasswordWaitingForFocusd;

    private void log(AccessibilityNodeInfo node, int level) {
        if (node == null) {
            return;
        }
        logd(level + " node:" + node.getChildCount() + node.getViewIdResourceName());
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo n = node.getChild(i);
            log(n, level + 1);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        logi("onAccessibilityEvent:" + event);
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            logv("No source node");
            return;
        } else {
            log(source, 0);
            if (AccessibilityEvent.TYPE_VIEW_CLICKED == event.getEventType()
                    && "com.netease.mobimail:id/domain_163".equals(source.getViewIdResourceName())) {
                m163Clicked = true;
                return;
            }
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
                if ("com.netease.mobimail.activity.LoginActivity".equals(event.getClassName())) {
                    m163Clicked = false;
                    return;
                } else {
                    AccessibilityNodeInfo account = findNode(source, new String[] {
                        "com.netease.mobimail:id/editor_email"
                    }, "");
                    AccessibilityNodeInfo password = findNode(source, new String[] {
                        "com.netease.mobimail:id/editor_password"
                    }, "");
                    if (m163Clicked && account != null && password != null) {
                        ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        mgr.setText("liyan12167");
                        account.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        password.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        mPasswordWaitingForFocusd = true;
                        return;
                    }
                }
                // AccessibilityNodeInfo node = findNode(source, new
                // String[]{"com.netease.mobimail:id/domain_163"}, "");
                // if (node != null) {
                //
                // }
                return;
            } else if (AccessibilityEvent.TYPE_VIEW_FOCUSED == event.getEventType()) {
                if (mPasswordWaitingForFocusd
                        && "com.netease.mobimail:id/editor_password".equals(source
                                .getViewIdResourceName())) {
                    ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    mgr.setText("Jz5855657");
                    source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    mPasswordWaitingForFocusd = false;
                }
            }
        }
        // if (event.getEventType() !=
        // AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        // return;
        // }
        // logd("AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED");
        // if (!"com.android.settings".equals(event.getPackageName())) {
        // logd("Non settings");
        // return;
        // }
        // if
        // ("com.android.settings.applications.InstalledAppDetailsTop".equals(event.getClassName()))
        // {
        // logd("enter InstalledAppDetailsTop");
        // AccessibilityNodeInfo node = findNode(source, new String[] {
        // "com.android.settings:id/left_button",
        // "com.android.settings:id/force_stop_button"
        // }, "强行停止");
        // if (node != null) {
        // if (mIsOkClicked) {
        // mIsOkClicked = false;
        // performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        // return;
        // }
        // if (node.isEnabled()) {
        // node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        // logi("perform 强行停止");
        // mIsStopClicked = true;
        // }
        // return;
        // }
        // } else if ("android.app.AlertDialog".equals(event.getClassName())) {
        // logd("enter AlertDialog");
        // AccessibilityNodeInfo node = findNode(source, new String[] {
        // "android:id/button1"
        // }, "确定");
        // if (node != null) {
        // if (mIsStopClicked) {
        // node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        // logi("perform 确定");
        // mIsOkClicked = true;
        // mIsStopClicked = false;
        // }
        // }
        // return;
        // }
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

    private static void logv(String msg) {
        Log.v(TAG, msg);
    }
}
