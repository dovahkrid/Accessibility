
package com.litan.accessibilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
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
import android.widget.TextView;
import android.widget.Toast;

import com.litan.accessibilitytest.AccessRecordManager.PerfomListener;
import com.litan.accessibilitytest.AccessRecordManager.RecordListener;

public class AccessibilityTestService extends AccessibilityService {
    private static final String TAG = "litan";
    private boolean mViewAdded;
    private AccessRecordManager mMgr;

    private void log(AccessibilityNodeInfo node, int level) {
        if (node == null) {
            return;
        }
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        logv(level + " node:" + node.getChildCount() + " " + node.getViewIdResourceName() + " "
                + node.getText() + " bounds:" + rect + " clickable:" + node.isClickable());
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo n = node.getChild(i);
            log(n, level + 1);
        }
    }

    private int mSizeRecordAdded;
    private AccessibilityNodeInfo mPassword;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        logi("onAccessibilityEvent:" + event.getWindowId() + " "
                + AccessibilityEvent.eventTypeToString(event.getEventType()));
        if (true) {
        	log(event.getSource(), 0);
        }
        // password manager
        if (!mStarted && !mPerform) {
        	if ("com.tencent.mobileqq".equals(event.getPackageName())) {
//        		if (AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY == event.getEventType()) {
//        			if (mPassword != null) {
//        				ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        				mgr.setText("ltltt585");
//        				boolean result = mPassword.performAction(AccessibilityNodeInfo.ACTION_PASTE);
//        				logv("litan ACTION_PASTE:" + result);
//        				mPassword = null;
//        				return;
//        			}
//        		}
        		AccessibilityNodeInfo source = event.getSource();
        		if (source != null) {
        			if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
        				List<AccessibilityNodeInfo> list = source.findAccessibilityNodeInfosByText("QQ号");
        				if (list != null && !list.isEmpty()) {
        					
        					AccessibilityNodeInfo login = list.get(0);
        					mPassword = findNode(source, new String[]{"com.tencent.mobileqq:id/password"}, null);
        					if (login != null && mPassword != null) {
        						login.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        						return;
        					}
        					return;
        				}
        			} else if (AccessibilityEvent.TYPE_VIEW_FOCUSED == event.getEventType()) {
        				//if ("QQ号/手机号/邮箱".equals(source.getText())) {
        				if ("请输入QQ号码或手机或邮箱".equals(source.getContentDescription())) {
        					if (source.getTextSelectionEnd() < 0) {
        						ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        						mgr.setText("396627398");
        						source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        						if (mPassword != null) {
        							mPassword.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        							mPassword = null;
        						}
        					}
        					return;
        				} else if ("com.tencent.mobileqq:id/password".equals(source.getViewIdResourceName())) {
        					int textEnd = source.getTextSelectionEnd();
        					if (textEnd < 0) {
        						ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	        					mgr.setText("ltltt585");
	        					source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        					}
//        					if (textEnd > 0) {
//        						Bundle bundle = new Bundle();
//        						bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 4);
//        						bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, source.getTextSelectionEnd());
//        						boolean result = source.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION);
//        					} else {
//	        					ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//	        					mgr.setText("ltltt585");
//	        					source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
//        					}
        					return;
        				}
        			} 
//        			else if (AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED == event.getEventType()) {
//        				if ("com.tencent.mobileqq:id/password".equals(source.getViewIdResourceName())) {
//        					Bundle arguments = new Bundle();
//    					   arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
//    					           AccessibilityNodeInfo.MOVEMENT_GRANULARITY_LINE);
//    					   arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
//    					           true);
//    					   source.performAction(AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY, arguments);
//        				}
//        			} 
        		}
        	}
        }
        
        if (TextUtils.isEmpty(mCurPkg)) {
            logv("onAccessibilityEvent:cant not find curPkg");
            log(event.getSource(), 0);
            return;
        }
        int type = event.getEventType();
        if (AccessibilityEvent.TYPE_VIEW_CLICKED == type
                || AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == type
                || AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == type
                || AccessibilityEvent.TYPE_VIEW_SCROLLED == type) {
            String eventPkgName = event.getPackageName().toString();
            log(event.getSource(), 0);
            if (mStarted) {
                if (eventPkgName.equals(mCurPkg)) {
                    boolean result = mMgr.record(event);
                    if (result) {
                        mTextView.setText("record " + ++mSizeRecordAdded);
                    }
                } else {
                    loge("onAccessibilityEvent(RECORD:event pkg name not consistent curPkgName:"
                            + mCurPkg + " event:" + eventPkgName);
                }
            } else if (mPerform) {
                if (eventPkgName.equals(mCurPkg)) {
                    mMgr.perfrom(event);
                } else {
                    loge("onAccessibilityEvent(PERFORM):event pkg name not consistent curPkgName:"
                            + mCurPkg + " event:" + eventPkgName);
                }
            }
        } else {
            logw("onAccessibilityEvent:unsupported type:"
                    + AccessibilityEvent.eventTypeToString(type));
        }
    }

    public static AccessibilityNodeInfo findNode(AccessibilityNodeInfo source, String[] ids,
            String text) {
        if (source == null) {
            throw new NullPointerException();
        }
        List<AccessibilityNodeInfo> nodeList = null;
        if (Build.VERSION.SDK_INT >= 18 && ids != null) {
            for (String id : ids) {
                if (id == null) {
                    continue;
                }
                nodeList = source
                        .findAccessibilityNodeInfosByViewId(id);
                if (nodeList != null && !nodeList.isEmpty()) {
                	if (text != null) {
                		for (AccessibilityNodeInfo node : nodeList) {
                			if (text.equals(node.getText().toString())) {
                				return node;
                			}
                		}
                	} else {
                		return nodeList.get(0);
                	}
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 14 && text != null) {
            nodeList = source.findAccessibilityNodeInfosByText(text);
            if (nodeList != null && !nodeList.isEmpty()) {
            	for (AccessibilityNodeInfo node : nodeList) {
            		if (text.equals(node.getText().toString())) {
            			return node;
            		}
            	}
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
    private TextView mTextView;

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
                        toast.setText("complete clicked");
                        mCurPkg = "";
                        mMgr.recordComplete();
                        mStarted = false;
                        mSizeRecordAdded = 0;
                        mTextView.setText("");
                        mWindowManager.removeView(mLinearLayout);
                        mViewAdded = false;
                        break;
                    case 1:
                        toast.setText("cancel clicked");
                        mStarted = false;
                        mSizeRecordAdded = 0;
                        mTextView.setText("");
                        mWindowManager.removeView(mLinearLayout);
                        mViewAdded = false;
                        mCurPkg = "";
                        mMgr.cancel();
                        break;
                }
                toast.show();
            }
        };
        Button ok = new Button(this);
        ok.setText("complete");
        ok.setId(0);
        ok.setOnClickListener(listener);
        Button cancel = new Button(this);
        cancel.setText("cancel");
        cancel.setId(1);
        cancel.setOnClickListener(listener);
        mTextView = new TextView(this);
        mLinearLayout.addView(ok);
        mLinearLayout.addView(cancel);
        mLinearLayout.addView(mTextView);
        mLinearLayout.setBackgroundColor(Color.BLUE);
        mLinearLayout.setOnTouchListener(new OnTouchListener() {
            float[] temp = new float[] {
                    0f, 0f
            };
            int viewWidth = mLinearLayout.getWidth();
            int viewX;
            int mStatusBarHeight = 0;
            float mSlop = ViewConfiguration.get(AccessibilityTestService.this).getScaledTouchSlop();

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
                        break;
                    case MotionEvent.ACTION_MOVE:
                        viewWidth = mLinearLayout.getWidth();
                        viewX = (int) (event.getRawX() - viewWidth / 2);
                        refreshWindow(viewX, (int) (event.getRawY()), true);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
    }

    private int mScreenHeight;
    private String mCurPkg = "";

    @Override
    public void onCreate() {
    	mMgr = new AccessRecordManager.AccessRecordManagerImpl(this);
        initWindowManager();
    }

    private class AccessTestService extends IAccessTestService.Stub {

        @Override
        public void startRecord(final String pkg) throws RemoteException {
            AccessibilityServiceInfo serviceInfo = getServiceInfo();
            serviceInfo.packageNames = new String[] {
                pkg
            };
            setServiceInfo(serviceInfo);
            if (mMgr.prepareRecord(pkg, new RecordListener() {

				@Override
				public void onInterrupt() {
					mCurPkg = "";
					mStarted = false;
					mSizeRecordAdded = 0;
					mTextView.setText("");
					if (mViewAdded) {
						mWindowManager.removeView(mLinearLayout);
						mViewAdded = false;
					}
					Toast.makeText(AccessibilityTestService.this,
							"record onInterrupt for pkg:" + pkg, Toast.LENGTH_SHORT).show();
				}
			})) {
            	mCurPkg = pkg;
            	mStarted = true;
                mPerform = false;
                mSizeRecordAdded = 0;
                if (mViewAdded) {
                    Toast.makeText(AccessibilityTestService.this, "already showed", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mWindowManager.addView(mLinearLayout, mLayoutParams);
                    mViewAdded = true;
                }
			} else {
				Toast.makeText(AccessibilityTestService.this,
						"can not start record", Toast.LENGTH_SHORT).show();
			}
        }

        @Override
        public List<String> getRecordedPkg() throws RemoteException {
            return mMgr.getRecored();
        }

        @Override
        public void startPerform(String pkg) throws RemoteException {
            if (mStarted) {
                Toast.makeText(AccessibilityTestService.this, "already in record time",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (mPerform) {
//                Toast.makeText(AccessibilityTestService.this, "already in perform time",
//                        Toast.LENGTH_SHORT).show();
            	mMgr.interrupt();
//                return;
            }
            mPerform = true;
            mCurPkg = pkg;
            boolean success = mMgr.preparePerform(pkg, new PerfomListener() {

                @Override
                public void onComplete() {
                    Toast.makeText(AccessibilityTestService.this, "Perform complete",
                            Toast.LENGTH_SHORT).show();
                    mPerform = false;
                    mCurPkg = "";
                }
            });
//            Toast.makeText(AccessibilityTestService.this,
//                    "preparePerform " + (success ? "success" : "failed"), Toast.LENGTH_SHORT)
//                    .show();
        }

    }

    @Override
    public void onServiceConnected() {
        logi("onServiceConnected");
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("action.binder");
        Bundle bundle = new Bundle();
        bundle.putBinder("binder", new AccessTestService());
        intent.putExtra("bundle", bundle);
        mgr.sendBroadcast(intent);
        List<String> pkgs = mMgr.getRecored();
        if (!pkgs.isEmpty()) {
        	AccessibilityServiceInfo info = getServiceInfo();
        	info.packageNames = pkgs.toArray(new String[]{});
        	setServiceInfo(info);
        }
    }

    static void logd(String msg) {
        Log.d(TAG, msg);
    }

    static void logw(String msg) {
        Log.w(TAG, msg);
    }

    static void loge(String msg) {
        Log.e(TAG, msg);
    }

    static void logi(String msg) {
        Log.i(TAG, msg);
    }

    static void logv(String msg) {
        Log.v(TAG, msg);
    }
}
