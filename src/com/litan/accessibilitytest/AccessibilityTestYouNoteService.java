package com.litan.accessibilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityTestYouNoteService extends AccessibilityService {
	private static void logv(String msg) {
		Log.v("litan", msg);
	}

	private static void logi(String msg) {
		Log.i("litan", msg);
	}

	private void log(AccessibilityNodeInfo node, int level) {
		if (node == null) {
			return;
		}
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		logv(level + " node:" + node.getChildCount() + " "
				+ node.getViewIdResourceName() + " " + node.getText()
				+ " bounds:" + rect);
		for (int i = 0; i < node.getChildCount(); i++) {
			AccessibilityNodeInfo n = node.getChild(i);
			log(n, level + 1);
		}
	}

	boolean mMoreClicked;
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		logi("Event:" + event);
		AccessibilityNodeInfo source = event.getSource();
		log(source, 0);
		if (source != null) {
			if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event
					.getEventType()) {
				List<AccessibilityNodeInfo> list = source
						.findAccessibilityNodeInfosByViewId("com.youdao.note:id/tab_text");
				for (AccessibilityNodeInfo n : list) {
					if ("更多".equals(n.getText().toString())) {
						n.getParent().performAction(
								AccessibilityNodeInfo.ACTION_CLICK);
						mMoreClicked = true;
						return;
					}
				}
			} else if (AccessibilityEvent.TYPE_VIEW_SCROLLED == event
					.getEventType()) {
				List<AccessibilityNodeInfo> list = source
						.findAccessibilityNodeInfosByViewId("com.youdao.note:id/sign_in_button");
				if (!list.isEmpty() && mMoreClicked) {
					boolean result = list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
					logi("sign_in_button click:" + result);
					mMoreClicked = false;
				}
			}
		}

	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

}
