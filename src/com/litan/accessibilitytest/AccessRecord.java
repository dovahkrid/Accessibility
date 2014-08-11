package com.litan.accessibilitytest;

import android.graphics.Rect;

public interface AccessRecord {
	int getChildDepth();
    int getEventType();
    CharSequence getPkgName();
   String getViewResName();
   CharSequence getContentDescription();
   CharSequence getText();
   int getWindowIndex();
   Rect getBoundsInScreen();
}
