
package com.litan.accessibilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AccessibilityTestService extends AccessibilityService {
    private static final String TAG = "litan";
    private boolean m163Clicked;
    private boolean mPasswordWaitingForFocusd;
    private boolean mViewAdded;

    private void log(AccessibilityNodeInfo node, int level) {
        if (node == null) {
            return;
        }
        logv(level + " node:" + node.getChildCount() + node);
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
                        if (!mViewAdded) {
                            mWindowManager.addView(mLinearLayout, mLayoutParams);
                        }
                        mViewAdded = true;

                        // ClipboardManager mgr = (ClipboardManager)
                        // getSystemService(Context.CLIPBOARD_SERVICE);
                        // mgr.setText("liyan12167");
                        // account.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        // password.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        // mPasswordWaitingForFocusd = true;
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
            } else if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == event.getEventType()) {
//                if (source.isScrollable()) {
//                   boolean success =  source.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
//                   logi("litan ACTION_SCROLL_FORWARD:" + success);
//                }
            }
        }
    }

    public static  AccessibilityNodeInfo findNode(AccessibilityNodeInfo source, String[] ids, String text) {
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

    private WindowManager mWindowManager;
    private LayoutParams mLayoutParams;
    private LinearLayout mLinearLayout;
    boolean mStarted;

    private void initWindowManager() {
        mWindowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(500,
                160, LayoutParams.TYPE_SYSTEM_ALERT,
                LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = mWindowManager.getDefaultDisplay().getWidth() / 2;
        mLayoutParams.y = mWindowManager.getDefaultDisplay().getHeight() / 2;
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(AccessibilityTestService.this, "", Toast.LENGTH_SHORT);
                switch (v.getId()) {
                    case 0:
                        Button bt = (Button) v;
                        if (mStarted) {
                            toast.setText("stop clicked");
                            bt.setText("start");
                            mStarted = false;
                            mWindowManager.removeView(mLinearLayout);
                            mViewAdded = false;
                        } else {
                            toast.setText("start clicked");
                            bt.setText("stop");
                            mStarted = true;
                        }
                        break;
                    case 1:
                        toast.setText("cancel clicked");
                        mWindowManager.removeView(mLinearLayout);
                        mViewAdded = false;
                        break;
                }
                toast.show();
            }
        };
        Button ok = new Button(this);
        ok.setText("start");
        ok.setId(0);
        ok.setOnClickListener(listener);
        Button cancel = new Button(this);
        cancel.setText("cancel");
        cancel.setId(1);
        cancel.setOnClickListener(listener);
        mLinearLayout.addView(ok);
        mLinearLayout.addView(cancel);
        mLinearLayout.setBackgroundColor(Color.BLUE);
        mLinearLayout.setOnTouchListener(new OnTouchListener() {
            boolean isMove = false;
            float[] temp = new float[] {
                    0f, 0f
            };
            int viewWidth = mLinearLayout.getWidth();
            int viewX;
            int mStatusBarHeight = 0;
            float mSlop = ViewConfiguration.get(AccessibilityTestService.this).getScaledTouchSlop();

            private boolean pointInView(View view, float localX, float localY) {
                return localX >= -mSlop && localY >= -mSlop
                        && localX < ((view.getRight() - view.getLeft()) + mSlop)
                        && localY < ((view.getBottom() - view.getTop()) + mSlop);
            }

            public void refreshWindow(int x, int y, boolean isAdjust) {
                if (mStatusBarHeight == 0) {
                    View rootView = mLinearLayout.getRootView();
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    mStatusBarHeight = r.top;
                }

                int height = 0;
                if (isAdjust) {
                    height = y - mStatusBarHeight;
                } else {
                    height = y;
                }
                if (height > mScreenHeight - mStatusBarHeight) {
                    height = mScreenHeight - mStatusBarHeight;
                }
                if (height <= 0) {
                    height = 0;
                }
                mLayoutParams.x = x;
                mLayoutParams.y = height;

                if (mViewAdded) {
                    try {
                        mWindowManager.updateViewLayout(mLinearLayout, mLayoutParams);
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN:
                        temp[0] = event.getX();
                        temp[1] = event.getY();
                        // isPressed = true;
                        isMove = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isMove || !pointInView(mLinearLayout, event.getX(), event.getY())) {
                            isMove = true;
                            // isPressed = false;
                            viewWidth = mLinearLayout.getWidth();
                            viewX = (int) (event.getRawX() - viewWidth / 2);
                            refreshWindow(viewX, (int) (event.getRawY()), true);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
        mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
    }

    private int mScreenWidth, mScreenHeight;
private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mViewAdded) {
            Toast.makeText(AccessibilityTestService.this, "already showed", Toast.LENGTH_SHORT).show();;
        } else {
            mWindowManager.addView(mLinearLayout, mLayoutParams);
            mViewAdded = true;
        }
    }
};
    @Override
    public void onCreate() {
        initWindowManager();
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("show.window");
        mgr.registerReceiver(mReceiver,  filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.unregisterReceiver(mReceiver);
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
