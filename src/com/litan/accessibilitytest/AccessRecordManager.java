package com.litan.accessibilitytest;

import java.util.LinkedList;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessRecordManager {
    boolean record(AccessibilityEvent event);
    //====perform====
    boolean perfrom(AccessibilityEvent event);
//    boolean readyForPerform(AccessibilityEvent event);
    public class AccessRecordManagerImpl implements AccessRecordManager {
        private LinkedList<AccessRecord> mRecordStack = new LinkedList<AccessRecord>();
        
        // 根据视图点击，以及引起的窗口内容变化并再遍历下一步需要点击的内容来创建记录
        @Override
        public boolean record(AccessibilityEvent event) {
            int type = event.getEventType();
            if (AccessibilityEvent.TYPE_VIEW_CLICKED == type) {
                AccessibilityNodeInfo nodeInfo = event.getSource();
                if (nodeInfo != null) {
                    String viewResName = nodeInfo.getViewIdResourceName();
                    CharSequence viewText = nodeInfo.getText();
                    if (viewResName != null && viewText != null) {
                        AccessRecordImpl record = new AccessRecordImpl();
                        record.mViewResName = viewResName;
                        record.mText = viewText;
                        record.mPkgName = nodeInfo.getPackageName();
                        record.mEventType = type;
                        mRecordStack.add(record);
                        return true;
                    }
                }
            }
            return false;
        }
        // ================================perform======================

        @Override
        public boolean perfrom(AccessibilityEvent event) {
            if (mRecordStack.isEmpty()) {
                return false;
            }
            AccessibilityNodeInfo source = event.getSource();
            if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == event.getEventType() && source != null) {
                AccessRecord record = mRecordStack.peekFirst();
                if (record.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    if (record.getPkgName().equals(event.getPackageName())) {
                        AccessibilityNodeInfo node = AccessibilityTestService.findNode(source, new String[]{record.getViewResName()}, null);
                        if (node != null) {
                            mRecordStack.remove();
                            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
            return false;
        }
       
    }
}
