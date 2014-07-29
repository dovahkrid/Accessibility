
package com.litan.accessibilitytest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessRecordManager {
    void senEnabledForRecord(boolean enabled);

    boolean record(AccessibilityEvent event);

    void cancel();

    // ====perform====
    boolean hasRecords();

    boolean perfrom(AccessibilityEvent event);

    void senEnabledForPerform(boolean enabled);

    // boolean readyForPerform(AccessibilityEvent event);
    public class AccessRecordManagerImpl implements AccessRecordManager {
        private LinkedList<AccessRecord> mRecordStack = new LinkedList<AccessRecord>();
        private HashMap<Integer, Integer> mWindowMap = new HashMap<Integer, Integer>();
        private boolean mEnabledForRecord;
        private boolean mEnabledForPerform;
        private int mWindowId;
        private int mWindowIndex;

        @Override
        public void senEnabledForRecord(boolean enabled) {
            mEnabledForRecord = enabled;
        }

        @Override
        public void cancel() {
            mRecordStack.clear();
        }

        // 根据视图点击，以及引起的窗口内容变化并再遍历下一步需要点击的内容来创建记录
        @Override
        public boolean record(AccessibilityEvent event) {
            // if (!mEnabledForRecord) {
            // return false;
            // }
            int type = event.getEventType();
            if (AccessibilityEvent.TYPE_VIEW_CLICKED == type) {
                AccessibilityNodeInfo nodeInfo = event.getSource();
                if (nodeInfo != null) {
                    String viewResName = nodeInfo.getViewIdResourceName();
                    CharSequence viewText = nodeInfo.getText();
                    if (viewResName != null) {
                        AccessRecordImpl record = new AccessRecordImpl();
                        record.mViewResName = viewResName;
                        record.mText = viewText;
                        record.mPkgName = nodeInfo.getPackageName();
                        record.mEventType = type;
                        Integer windowIndex = mWindowMap.get(event.getWindowId());
                        record.mWindowIndex = windowIndex == null ? mWindowIndex : windowIndex;
                        mRecordStack.add(record);
                        return true;
                    }
                }
            } else if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == type) {
                int newWindowId = event.getWindowId();
                if (mWindowId != newWindowId) {
                    mWindowId = newWindowId;
                    mWindowMap.put(newWindowId, ++mWindowIndex);
                }
            }
            return false;
        }

        // ================================perform======================
        private List<AccessibilityNodeInfo> mNodeList = new ArrayList<AccessibilityNodeInfo>();

        @Override
        public boolean perfrom(AccessibilityEvent event) {
            // if (!mEnabledForPerform) {
            // return false;
            // }
            if (mRecordStack.isEmpty()) {
                return false;
            }
            AccessibilityNodeInfo source = event.getSource();
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()
                    && source != null) {
                mNodeList.clear();
                AccessRecord record = mRecordStack.peekFirst();
                if (record.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    if (record.getPkgName().equals(event.getPackageName())) {
                        AccessibilityNodeInfo node = AccessibilityTestService.findNode(source,
                                new String[] {
                                    record.getViewResName()
                                }, null);
                        if (node != null) {
                            mRecordStack.remove();
                            int windowIndex = record.getWindowIndex();
                            List<AccessRecord> sameWindowIndex = new ArrayList<AccessRecord>();
                            for (AccessRecord r : mRecordStack) {
                                if (windowIndex == r.getWindowIndex()) {
                                    sameWindowIndex.add(r);
                                    AccessibilityNodeInfo n = AccessibilityTestService.findNode(
                                            source,
                                            new String[] {
                                                r.getViewResName()
                                            }, null);
                                    if (n != null) {
                                        mNodeList.add(n);
                                    }
                                }
                            }
                            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            } else if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == event.getEventType()) {
                AccessibilityNodeInfo node = null;
                AccessRecord record = mRecordStack.peekFirst();
                for (AccessibilityNodeInfo n : mNodeList) {
                    String recordRes = record.getViewResName();
                    String nodeRes = n.getViewIdResourceName();
                    CharSequence recordText = record.getText();
                    CharSequence nodeText = n.getText();
                    if (recordRes != null && nodeRes != null) {
                        if (recordRes.equals(nodeRes)) {
                            if ((recordText == null && nodeText == null)
                                    || (recordText != null && recordText.equals(nodeText))) {
                                node = n;
                                break;
                            }
                        }
                    }
                }
                if (node != null) {
                    mRecordStack.remove();
                    mNodeList.remove(node);
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            return false;
        }

        @Override
        public void senEnabledForPerform(boolean enabled) {
            mEnabledForPerform = enabled;
        }

        @Override
        public boolean hasRecords() {
            return !mRecordStack.isEmpty();
        }
    }
}
