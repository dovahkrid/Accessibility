
package com.litan.accessibilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
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
    private boolean mViewAdded;
    private AccessRecordManager mMgr = new AccessRecordManager.AccessRecordManagerImpl();

    private class MyButton extends Button {

        public MyButton(Context context) {
            super(context);
        }

//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//            boolean result = super.onTouchEvent(event);
//            //mLinearLayout.onTouchEvent(event);
//            return false;
//        }

    }

    private void log(AccessibilityNodeInfo node, int level) {
        if (node == null) {
            return;
        }
        logv(level + " node:" + node.getChildCount() + " " + node.getViewIdResourceName() + " " + node.getText());
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo n = node.getChild(i);
            log(n, level + 1);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        logi("onAccessibilityEvent:" + event.getWindowId() + " " + event);
        log(event.getSource(), 0);
        if (mStarted) {
            mMgr.record(event);
            return;
        }
        if (mPerform) {
            mMgr.perfrom(event);
            if (!mMgr.hasRecords()) {
                mPerform = false;
            }
        }
        // AccessibilityNodeInfo source = event.getSource();
        // if (source == null) {
        // logv("No source node");
        // return;
        // } else {
        // log(source, 0);
        // if (AccessibilityEvent.TYPE_VIEW_CLICKED == event.getEventType()
        // &&
        // "com.netease.mobimail:id/domain_163".equals(source.getViewIdResourceName()))
        // {
        // m163Clicked = true;
        // return;
        // }
        // if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ==
        // event.getEventType()) {
        // if
        // ("com.netease.mobimail.activity.LoginActivity".equals(event.getClassName()))
        // {
        // m163Clicked = false;
        // return;
        // } else {
        // AccessibilityNodeInfo account = findNode(source, new String[] {
        // "com.netease.mobimail:id/editor_email"
        // }, "");
        // AccessibilityNodeInfo password = findNode(source, new String[] {
        // "com.netease.mobimail:id/editor_password"
        // }, "");
        // if (m163Clicked && account != null && password != null) {
        // if (!mViewAdded) {
        // mWindowManager.addView(mLinearLayout, mLayoutParams);
        // }
        // mViewAdded = true;
        //
        // // ClipboardManager mgr = (ClipboardManager)
        // // getSystemService(Context.CLIPBOARD_SERVICE);
        // // mgr.setText("liyan12167");
        // // account.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        // // password.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        // // mPasswordWaitingForFocusd = true;
        // return;
        // }
        // }
        // // AccessibilityNodeInfo node = findNode(source, new
        // // String[]{"com.netease.mobimail:id/domain_163"}, "");
        // // if (node != null) {
        // //
        // // }
        // return;
        // } else if (AccessibilityEvent.TYPE_VIEW_FOCUSED ==
        // event.getEventType()) {
        // if (mPasswordWaitingForFocusd
        // && "com.netease.mobimail:id/editor_password".equals(source
        // .getViewIdResourceName())) {
        // ClipboardManager mgr = (ClipboardManager)
        // getSystemService(Context.CLIPBOARD_SERVICE);
        // mgr.setText("Jz5855657");
        // source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        // mPasswordWaitingForFocusd = false;
        // }
        // } else if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ==
        // event.getEventType()) {
        // // if (source.isScrollable()) {
        // // boolean success =
        // source.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        // // logi("litan ACTION_SCROLL_FORWARD:" + success);
        // // }
        // }
        // }
    }

    public static AccessibilityNodeInfo findNode(AccessibilityNodeInfo source, String[] ids,
            String text) {
        if (source == null) {
            throw new NullPointerException();
        }
        List<AccessibilityNodeInfo> nodeList = null;
        if (Build.VERSION.SDK_INT >= 18 && ids != null) {
                for (String id : ids) {
                    nodeList = source
                            .findAccessibilityNodeInfosByViewId(id);
                    if (nodeList != null && !nodeList.isEmpty()) {
                        return nodeList.get(0);
                    }
                }
        }
        if (Build.VERSION.SDK_INT >= 14 && text != null) {
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
    boolean mPerform;

    private void initWindowManager() {
        mWindowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_SYSTEM_ALERT,
                LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = mWindowManager.getDefaultDisplay().getWidth() / 2;
        mLayoutParams.y = mWindowManager.getDefaultDisplay().getHeight() / 2;
        mLinearLayout = new LinearLayout(this) {

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    logv("LinearLayout onInterceptTouchEvent true");
                    return true;
                }
                logv("LinearLayout onInterceptTouchEvent false");
                return super.onInterceptTouchEvent(event);
            }
            
        };
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
                        mMgr.cancel();
                        break;
                    case 2:
                        toast.setText("perform clicked");
                        mPerform = true;
                }
                toast.show();
            }
        };
        Button ok = new MyButton(this);
        ok.setText("start");
        ok.setId(0);
        ok.setOnClickListener(listener);
        Button cancel = new MyButton(this);
        cancel.setText("cancel");
        cancel.setId(1);
        cancel.setOnClickListener(listener);
        Button perform = new MyButton(this);
        perform.setText("perfrom");
        perform.setId(2);
        perform.setOnClickListener(listener);
        mLinearLayout.addView(ok);
        mLinearLayout.addView(cancel);
        mLinearLayout.addView(perform);
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
                logv("LinearLayout onTouch " + event);
                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN:
                        temp[0] = event.getX();
                        temp[1] = event.getY();
                        // isPressed = true;
                        isMove = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //if (isMove || !pointInView(mLinearLayout, event.getX(), event.getY())) {
                            isMove = true;
                            // isPressed = false;
                            viewWidth = mLinearLayout.getWidth();
                            viewX = (int) (event.getRawX() - viewWidth / 2);
                            refreshWindow(viewX, (int) (event.getRawY()), true);
                        //}
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
                Toast.makeText(AccessibilityTestService.this, "already showed", Toast.LENGTH_SHORT)
                        .show();
                ;
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
        mgr.registerReceiver(mReceiver, filter);
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
        AccessibilityServiceInfo serviceInfo = getServiceInfo();
        logi("serviceInfo:" + serviceInfo);
        serviceInfo.packageNames = new String[]{"com.android.settings"};
        setServiceInfo(serviceInfo);
        logi("set serviceInfo:" + getServiceInfo());
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
