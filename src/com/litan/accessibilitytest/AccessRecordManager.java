
package com.litan.accessibilitytest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessRecordManager {
	interface RecordListener {
		void onInterrupt();
	}
    void senEnabledForRecord(boolean enabled);

    List<String> getRecored();

    boolean prepareRecord(String pkg, RecordListener listener);
    boolean record(AccessibilityEvent event);

    void interrupt();

    void recordComplete();

    void cancel();

    boolean isRecording();

    // ====perform====
    interface PerfomListener {
        void onComplete();
    }

    boolean hasRecords();

    boolean preparePerform(String pkg, PerfomListener listener);

    boolean perfrom(AccessibilityEvent event);

    void senEnabledForPerform(boolean enabled);

    // boolean readyForPerform(AccessibilityEvent event);
    public class AccessRecordManagerImpl implements AccessRecordManager {
        private Map<String, LinkedList<AccessRecord>> mRecordMap = new HashMap<String, LinkedList<AccessRecord>>();
        private String mCurPkg;
        private RecordListener mRecordListener;
        private LinkedList<AccessRecord> mCurRecordList = new LinkedList<AccessRecord>();
        private HashMap<Integer, WindowNode> mWindowMap = new HashMap<Integer, WindowNode>();
        // private boolean mEnabledForRecord;
        // private boolean mEnabledForPerform;
        private int mWindowId;
        private int mWindowIndex;
        private class WindowNode {
        	int index;
        	AccessibilityNodeInfo windowNode;
        	List<AccessibilityNodeInfo> contentNodes = new ArrayList<AccessibilityNodeInfo>();
        }
        public boolean prepareRecord(String pkg, RecordListener listener) {
        	mCurPkg = pkg;
        	mRecordListener = listener;
        	return true;
        }
        public void interrupt() {
        	AccessibilityTestService.logi("interrupt for pkg:" + mCurPkg);
            mCurPkg = null;
            mCurRecordList.clear();
            mWindowId = -1;
            mWindowMap.clear();
            mWindowIndex = -1;
        }

        public boolean isRecording() {
            return false;
        }

        public void recordComplete() {
            if (mCurPkg != null && !mCurRecordList.isEmpty()) {
                mRecordMap.put(mCurPkg, (LinkedList<AccessRecord>) mCurRecordList.clone());
                interrupt();
            } else {
                AccessibilityTestService.loge("recordComplete: failed with curPkg:" + mCurPkg
                        + " recordList.size:" + mCurRecordList.size());
            }
        }

        @Override
        public void senEnabledForRecord(boolean enabled) {
            // mEnabledForRecord = enabled;
        }

        @Override
        public List<String> getRecored() {
            List<String> list = new ArrayList<String>();
            list.addAll(mRecordMap.keySet());
            return list;
        }

        @Override
        public void cancel() {
            interrupt();
            mRecordMap.clear();
        }

        // 根据视图点击，以及引起的窗口内容变化并再遍历下一步需要点击的内容来创建记录
        @Override
        public boolean record(AccessibilityEvent event) {
            // if (!mEnabledForRecord) {
            // return false;
            // }
            if (mCurPkg == null) {
                mCurPkg = event.getPackageName().toString();
                if (mRecordMap.containsKey(mCurPkg)) {
                    AccessibilityTestService.logw("record: pkg:" + mCurPkg
                            + " already had recorded, now clear the records.");
                    mRecordMap.remove(mCurPkg);
                }
            }
            int type = event.getEventType();
            if (AccessibilityEvent.TYPE_VIEW_CLICKED == type) {
                AccessibilityNodeInfo nodeInfo = event.getSource();
                if (nodeInfo != null) {
                    String viewResName = nodeInfo.getViewIdResourceName();
                    CharSequence viewText = nodeInfo.getText();
                    Rect boundsInScreen = new Rect();
                    nodeInfo.getBoundsInScreen(boundsInScreen);
                    // if (viewResName != null || viewText != null) {
                    WindowNode windowNode = mWindowMap.get(event.getWindowId());
                    if (windowNode == null) {
                    	AccessibilityTestService.loge("record: unknow window of CLICK EVENT windowNode:" + windowNode);
                    	interrupt();
                    	if (mRecordListener != null) {
                    		mRecordListener.onInterrupt();
                    	}
                    	return false;
                    }
//					AccessibilityNodeInfo n = findNode(windowNode.windowNode,
//							viewResName,
//							viewText == null ? null : viewText.toString(),
//							boundsInScreen, windowNode);
//					if (n == null) {
//						AccessibilityTestService
//								.loge("record: can not find click node from window node:"
//										+ windowNode.windowNode);
//						interrupt();
//						if (mRecordListener != null) {
//							mRecordListener.onInterrupt();
//						}
//						return false;
//					}
                    AccessRecordImpl record = new AccessRecordImpl();
                    record.mViewResName = viewResName;
                    record.mText = viewText;
                    record.mPkgName = nodeInfo.getPackageName();
                    record.mEventType = type;
                    record.mWindowIndex = windowNode.index;
                    record.mBoundsInScreen = boundsInScreen;
                    mCurRecordList.add(record);
                    AccessibilityTestService.logi("add Record:" + viewResName + " windowIndex:"
                            + record.mWindowIndex);
                    return true;
                } else {
                    AccessibilityTestService
                            .loge("record: cant find sourcenode for event:" + event);
                    interrupt();
                	if (mRecordListener != null) {
                		mRecordListener.onInterrupt();
                	}
                	return false;
                }
            } else if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == type) {
                int newWindowId = event.getWindowId();
                if (mWindowMap.containsKey(newWindowId)) {
                	WindowNode windowNode = mWindowMap.get(newWindowId);
                    int windowIndex = windowNode.index;
                    mWindowIndex = windowIndex;
                    mWindowId = newWindowId;
                    AccessibilityTestService.logd("record:got old window index:" + windowIndex
                            + " for window id:" + newWindowId);
                } else {
                    if (mWindowId != newWindowId) {
                        AccessibilityTestService.logd("record:new Window id:" + newWindowId
                                + " and oldWindow id:" + mWindowId + " oldWindowIndex:"
                                + mWindowIndex);
                        AccessibilityNodeInfo n = event.getSource();
                        if (n == null) {
                        	AccessibilityTestService
                            .loge("record: new window with null sourcenode");
                        	interrupt();
                        	if (mRecordListener != null) {
                        		mRecordListener.onInterrupt();
                        	}
                        	return false;
                        }
                        mWindowId = newWindowId;
                        WindowNode windowNode = new WindowNode();
                        windowNode.index = ++mWindowIndex;
                        windowNode.windowNode = event.getSource();
                        mWindowMap.put(newWindowId, windowNode);
                    }
                }
            } else if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == type) {
            	WindowNode windowNode = mWindowMap.get(event.getWindowId());
            	if (windowNode == null) {
            		AccessibilityTestService.loge("record: can not find window node for TYPE_WINDOW_CONTENT_CHANGED");
            		interrupt();
                	if (mRecordListener != null) {
                		mRecordListener.onInterrupt();
                	}
                	return false;
            	}
            	windowNode.contentNodes.add(event.getSource());
            }
            return false;
        }

        // ================================perform======================
        private List<AccessibilityNodeInfo> mNodeList = new ArrayList<AccessibilityNodeInfo>();
        private LinkedList<AccessRecord> mCurPerformList;

        private AccessibilityNodeInfo findNode(AccessibilityNodeInfo source, Rect rect) {
            Rect r = new Rect();
            source.getBoundsInScreen(r);
            if (r.equals(rect)) {
                return source;
            } else {
                for (int i = 0; i < source.getChildCount(); i++) {
                    AccessibilityNodeInfo node = source.getChild(i);
                    if (node != null) {
                        AccessibilityNodeInfo n = findNode(node, rect);
                        if (n != null) {
                            return n;
                        }
                    }
                }
            }
            return null;
        }

        private AccessibilityNodeInfo findNode(AccessibilityNodeInfo source, String res, String text, Rect rect, WindowNode windowNode) {
        	AccessibilityNodeInfo result;
        	if (source == null) {
        		return null;
        	}
        	if (res == null && text == null) {
        		result = findNode(source, rect);
        		if (result == null && windowNode != null) {
        			result = findNode(windowNode.windowNode, rect);
        			if (result == null && windowNode.contentNodes != null) {
        				for (AccessibilityNodeInfo n : windowNode.contentNodes) {
        					result = findNode(n, rect);
        					if (result != null) {
        						return result;
        					}
        				}
        			}
        		}
        	} else {
        		String[] ids = res == null ? null : new String[]{res};
        		result = AccessibilityTestService.findNode(source, ids, text);
        		if (result == null && windowNode != null) {
        			result = AccessibilityTestService.findNode(windowNode.windowNode, ids, text);
        			if (result == null && windowNode.contentNodes != null) {
        				for (AccessibilityNodeInfo n : windowNode.contentNodes) {
        					result = AccessibilityTestService.findNode(n, ids, text);
        					if (result != null) {
        						return result;
        					}
        				}
        			}
        		}
        	}
        	return result;
        }
        @Override
        public boolean perfrom(AccessibilityEvent event) {
            // if (!mEnabledForPerform) {
            // return false;
            // }
            if (mCurPerformList == null || mCurPerformList.isEmpty()) {
                return false;
            }
            AccessibilityNodeInfo source = event.getSource();
            if (source == null) {
            	return false;
            }
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
                mNodeList.clear();
                AccessRecord record = mCurPerformList.peekFirst();
                if (record.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    if (record.getPkgName().equals(event.getPackageName())) {
                        String resName = record.getViewResName();
                        CharSequence text = record.getText();
                        AccessibilityNodeInfo node = findNode(source, resName, text != null ? text.toString() : null, record.getBoundsInScreen(), null);
                        if (node != null) {
                            mCurPerformList.remove();
                            int windowIndex = record.getWindowIndex();
                            //List<AccessRecord> sameWindowIndex = new ArrayList<AccessRecord>();
                            for (AccessRecord r : mCurPerformList) {
                                if (windowIndex == r.getWindowIndex()) {
                                    //sameWindowIndex.add(r);
                                    String rN = r.getViewResName();
                                    CharSequence rT = r.getText();
                                    AccessibilityNodeInfo n;
                                    if (rN == null && rT == null) {
                                        n = findNode(source, r.getBoundsInScreen());
                                    } else {
                                        n = AccessibilityTestService.findNode(
                                                source,
                                                rN == null ? null :
                                                        new String[] {
                                                                rN
                                                        }, rT == null ? null : rT.toString());
                                    }
                                    if (n != null) {
                                        mNodeList.add(n);
                                    } else {
                                        AccessibilityTestService
                                                .loge("perform: cant not find node res:" + rN
                                                        + " text:" + rT + " boundsInScreen:"
                                                        + r.getBoundsInScreen());
                                    }
                                }
                            }
                            AccessibilityTestService.logi("perform click record:"
                                    + record.getViewResName() + " windowIndex:" + windowIndex);
                            boolean result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            if (mCurPerformList.isEmpty()) {
                                if (mListener != null) {
                                    mListener.onComplete();
                                }
                            }
                            return result;
                        } else {
                            AccessibilityTestService.loge("perform: cant not find node res:"
                                    + resName + " text:" + text + " boundsInScreen:"
                                    + record.getBoundsInScreen());
                        }
                    }
                }
            } else if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == event.getEventType()) {
                AccessibilityNodeInfo node = null;
                AccessRecord record = mCurPerformList.peekFirst();
                for (AccessRecord r : mCurPerformList) {
                    if (record.getWindowIndex() == r.getWindowIndex()) {
                        String rN = r.getViewResName();
                        CharSequence rT = r.getText();
                        AccessibilityNodeInfo n;
                        if (rN == null && rT == null) {
                            n = findNode(source, r.getBoundsInScreen());
                        } else {
                            n = AccessibilityTestService.findNode(
                                    source,
                                    rN == null ? null :
                                            new String[] {
                                                    rN
                                            }, rT == null ? null : rT.toString());
                        }
                        if (n != null) {
                            mNodeList.add(n);
                        } else {
                            AccessibilityTestService
                                    .loge("perform: cant not find node res:" + rN
                                            + " text:" + rT + " boundsInScreen:"
                                            + r.getBoundsInScreen());
                        }
                    }
                }
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
                    Rect rect = new Rect();
                    n.getBoundsInScreen(rect);
                    if (rect.equals(record.getBoundsInScreen())) {
                    	node = n;
                    }
                }
                if (node != null) {
                    mCurPerformList.remove();
                    mNodeList.remove(node);
                    boolean result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (mCurPerformList.isEmpty()) {
                        if (mListener != null) {
                            mListener.onComplete();
                        }
                    }
                    return result;
                }
            }
            return false;
        }

        @Override
        public void senEnabledForPerform(boolean enabled) {
            // mEnabledForPerform = enabled;
        }

        @Override
        public boolean hasRecords() {
            return false;
            // return !mRecordList.isEmpty();
        }

        private PerfomListener mListener;

        @Override
        public boolean preparePerform(String pkg, PerfomListener listener) {
            LinkedList<AccessRecord> list = mRecordMap.get(pkg);
            if (list == null) {
                return false;
            }
            mCurPerformList = (LinkedList<AccessRecord>) list.clone();
            mListener = listener;
            return true;
        }
    }
}
