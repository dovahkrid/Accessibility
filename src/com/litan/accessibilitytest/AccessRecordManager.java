package com.litan.accessibilitytest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessRecordManager {
	void senEnabledForRecord(boolean enabled);

	List<String> getRecored();

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
		private LinkedList<AccessRecord> mCurRecordList = new LinkedList<AccessRecord>();
		private HashMap<Integer, Integer> mWindowMap = new HashMap<Integer, Integer>();
		// private boolean mEnabledForRecord;
		// private boolean mEnabledForPerform;
		private int mWindowId;
		private int mWindowIndex;

		public void interrupt() {
			mCurPkg = null;
			mCurRecordList.clear();
			mWindowId = -1;
			mWindowIndex = -1;
		}

		public boolean isRecording() {
			return false;
		}

		public void recordComplete() {
			if (mCurPkg != null && !mCurRecordList.isEmpty()) {
				mRecordMap.put(mCurPkg,
						(LinkedList<AccessRecord>) mCurRecordList.clone());
				interrupt();
			} else {
				AccessibilityTestService
						.loge("recordComplete: failed with curPkg:" + mCurPkg
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
					if (viewResName != null || viewText != null) {
						AccessRecordImpl record = new AccessRecordImpl();
						record.mViewResName = viewResName;
						record.mText = viewText;
						record.mPkgName = nodeInfo.getPackageName();
						record.mEventType = type;
						record.mWindowIndex = mWindowMap.get(event
								.getWindowId());
						mCurRecordList.add(record);
						AccessibilityTestService.logi("add Record:"
								+ viewResName + " windowIndex:"
								+ record.mWindowIndex);
						return true;
					} else {
						AccessibilityTestService
								.loge("record: cant find viewResName and text for node:"
										+ nodeInfo);
					}
				} else {
					AccessibilityTestService
							.loge("record: cant find sourcenode for event:"
									+ event);
				}
			} else if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == type) {
				int newWindowId = event.getWindowId();
				if (mWindowMap.containsKey(newWindowId)) {
					int windowIndex = mWindowMap.get(newWindowId);
					mWindowIndex = windowIndex;
					mWindowId = newWindowId;
					AccessibilityTestService
							.logd("record:got old window index:" + windowIndex
									+ " for window id:" + newWindowId);
				} else {
					if (mWindowId != newWindowId) {
						AccessibilityTestService
								.logd("record:new Window id:" + newWindowId
										+ " and oldWindow id:" + mWindowId
										+ " oldWindowIndex:" + mWindowIndex);
						mWindowId = newWindowId;
						mWindowMap.put(newWindowId, ++mWindowIndex);
					}
				}
			}
			return false;
		}

		// ================================perform======================
		private List<AccessibilityNodeInfo> mNodeList = new ArrayList<AccessibilityNodeInfo>();
		private LinkedList<AccessRecord> mCurPerformList;

		@Override
		public boolean perfrom(AccessibilityEvent event) {
			// if (!mEnabledForPerform) {
			// return false;
			// }
			if (mCurPerformList == null || mCurPerformList.isEmpty()) {
				return false;
			}
			AccessibilityNodeInfo source = event.getSource();
			if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event
					.getEventType() && source != null) {
				mNodeList.clear();
				AccessRecord record = mCurPerformList.peekFirst();
				if (record.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
					if (record.getPkgName().equals(event.getPackageName())) {
						String resName = record.getViewResName();
						CharSequence recordText = record.getText();
						AccessibilityNodeInfo node = AccessibilityTestService
								.findNode(
										source,
										resName == null ? null
												: new String[] { record
														.getViewResName() },
										recordText == null ? null : recordText
												.toString());
						if (node != null) {
							mCurPerformList.remove();
							int windowIndex = record.getWindowIndex();
							List<AccessRecord> sameWindowIndex = new ArrayList<AccessRecord>();
							for (AccessRecord r : mCurPerformList) {
								if (windowIndex == r.getWindowIndex()) {
									sameWindowIndex.add(r);
									String rN = r.getViewResName();
									CharSequence rT = r.getText();
									AccessibilityNodeInfo n = AccessibilityTestService
											.findNode(
													source,
													rN == null ? null
															: new String[] { r
																	.getViewResName() },
													rT == null ? null : rT
															.toString());
									if (n != null) {
										mNodeList.add(n);
									}
								}
							}
							AccessibilityTestService
									.logi("perform click record:"
											+ record.getViewResName()
											+ " windowIndex:" + windowIndex);
							boolean result = node
									.performAction(AccessibilityNodeInfo.ACTION_CLICK);
							if (mCurPerformList.isEmpty()) {
								if (mListener != null) {
									mListener.onComplete();
								}
							}
							return result;
						}
					}
				}
			} else if (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == event
					.getEventType()) {
				AccessibilityNodeInfo node = null;
				AccessRecord record = mCurPerformList.peekFirst();
				for (AccessibilityNodeInfo n : mNodeList) {
					String recordRes = record.getViewResName();
					String nodeRes = n.getViewIdResourceName();
					CharSequence recordText = record.getText();
					CharSequence nodeText = n.getText();
					if (recordRes != null && nodeRes != null) {
						if (recordRes.equals(nodeRes)) {
							if ((recordText == null && nodeText == null)
									|| (recordText != null && recordText
											.equals(nodeText))) {
								node = n;
								break;
							}
						}
					}
				}
				if (node != null) {
					mCurPerformList.remove();
					mNodeList.remove(node);
					boolean result = node
							.performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
